package com.exemplo.streaming;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// Testes de integração: aplicação completa, banco H2 em memória e requisições HTTP simuladas
@SpringBootTest
@AutoConfigureMockMvc
class StreamingApiIntegrationTest {

    private static final String CABECALHO = "X-Usuario-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RegistroHistoricoRepository historicoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private FilmeRepository filmeRepository;

    private Filme filmeInfantil;
    private Filme outroInfantil;
    private Filme filmeAdulto;
    private Filme filmePremium;

    @BeforeEach
    void prepararCenario() {
        // Limpa na ordem certa (quem tem chave estrangeira primeiro) e cadastra um catálogo próprio do teste
        historicoRepository.deleteAll();
        usuarioRepository.deleteAll();
        contaRepository.deleteAll();
        filmeRepository.deleteAll();

        filmeInfantil = filmeRepository.save(new Filme("Filme Infantil", Perfil.INFANTIL, Plano.BASICO));
        outroInfantil = filmeRepository.save(new Filme("Outro Infantil", Perfil.INFANTIL, Plano.BASICO));
        filmeRepository.save(new Filme("Filme Adolescente", Perfil.ADOLESCENTE, Plano.BASICO));
        filmeAdulto = filmeRepository.save(new Filme("Filme Adulto", Perfil.ADULTO, Plano.BASICO));
        filmePremium = filmeRepository.save(new Filme("Filme Premium", Perfil.INFANTIL, Plano.PREMIUM));
    }

    // ---------- Fluxo completo: perfil, bloqueio, histórico e repetições ----------

    @Test
    void fluxoCompleto_perfilInfantil_listaBloqueiaERegistraHistoricoComRepeticoes() throws Exception {
        JsonNode assinante = criarAssinante("Ana", "ana@email.com", "PADRAO");
        long assinanteId = assinante.get("id").asLong();
        long contaId = assinante.get("contaId").asLong();

        ResultActions criacao = criarNaoAssinante(contaId, assinanteId, "Joaozinho", "joao@email.com", "INFANTIL");
        criacao.andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("NAO_ASSINANTE"))
                .andExpect(jsonPath("$.perfil").value("INFANTIL"));
        long criancaId = lerJson(criacao.andReturn()).get("id").asLong();

        // Plano PADRAO + perfil INFANTIL: só os 2 filmes infantis do plano básico
        mockMvc.perform(get("/filmes").header(CABECALHO, criancaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].titulo").value("Filme Infantil"))
                .andExpect(jsonPath("$[1].titulo").value("Outro Infantil"));

        // Filme adulto é bloqueado para o perfil infantil
        mockMvc.perform(post("/filmes/{id}/assistir", filmeAdulto.getId()).header(CABECALHO, criancaId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").value(containsString("perfil")));

        // Assiste A, B e A de novo
        assistir(filmeInfantil, criancaId);
        assistir(outroInfantil, criancaId);
        assistir(filmeInfantil, criancaId);

        // O histórico mostra os 3 registros, na ordem em que foram assistidos (o filme repetido aparece duas vezes)
        mockMvc.perform(get("/usuarios/{id}/historico", criancaId).header(CABECALHO, criancaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].titulo").value("Filme Infantil"))
                .andExpect(jsonPath("$[1].titulo").value("Outro Infantil"))
                .andExpect(jsonPath("$[2].titulo").value("Filme Infantil"));

        // O assinante também pode ver o histórico da criança
        mockMvc.perform(get("/usuarios/{id}/historico", criancaId).header(CABECALHO, assinanteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    // ---------- Cota de compartilhamento ----------

    @Test
    void cota_planoBasicoPermiteUmNaoAssinante_oSegundoERecusado() throws Exception {
        JsonNode assinante = criarAssinante("Ana", "ana@email.com", "BASICO");
        long assinanteId = assinante.get("id").asLong();
        long contaId = assinante.get("contaId").asLong();

        criarNaoAssinante(contaId, assinanteId, "Primeiro", "primeiro@email.com", "ADULTO")
                .andExpect(status().isCreated());

        criarNaoAssinante(contaId, assinanteId, "Segundo", "segundo@email.com", "ADULTO")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value(containsString("Cota")));

        mockMvc.perform(get("/contas/{id}/cota", contaId).header(CABECALHO, assinanteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cotaTotal").value(1))
                .andExpect(jsonPath("$.cotaUsada").value(1))
                .andExpect(jsonPath("$.cotaDisponivel").value(0));
    }

    // ---------- Restrição ao assinante ----------

    @Test
    void naoAssinante_naoPodeAdicionarNemRemoverUsuarios() throws Exception {
        JsonNode assinante = criarAssinante("Ana", "ana@email.com", "PREMIUM");
        long assinanteId = assinante.get("id").asLong();
        long contaId = assinante.get("contaId").asLong();

        long filhoId = lerJson(criarNaoAssinante(contaId, assinanteId, "Filho", "filho@email.com", "ADOLESCENTE")
                .andReturn()).get("id").asLong();
        long outroId = lerJson(criarNaoAssinante(contaId, assinanteId, "Outro", "outro@email.com", "INFANTIL")
                .andReturn()).get("id").asLong();

        // O filho tenta adicionar um usuário
        criarNaoAssinante(contaId, filhoId, "Intruso", "intruso@email.com", "ADULTO")
                .andExpect(status().isForbidden());

        // O filho tenta remover o outro usuário
        mockMvc.perform(delete("/usuarios/{id}", outroId).header(CABECALHO, filhoId))
                .andExpect(status().isForbidden());
    }

    @Test
    void assinante_podeRemoverUmNaoAssinante() throws Exception {
        JsonNode assinante = criarAssinante("Ana", "ana@email.com", "PADRAO");
        long assinanteId = assinante.get("id").asLong();
        long contaId = assinante.get("contaId").asLong();
        long filhoId = lerJson(criarNaoAssinante(contaId, assinanteId, "Filho", "filho@email.com", "INFANTIL")
                .andReturn()).get("id").asLong();

        mockMvc.perform(delete("/usuarios/{id}", filhoId).header(CABECALHO, assinanteId))
                .andExpect(status().isNoContent());

        // Com o usuário removido, a cota volta a ficar livre
        mockMvc.perform(get("/contas/{id}/cota", contaId).header(CABECALHO, assinanteId))
                .andExpect(jsonPath("$.cotaUsada").value(0));
    }

    // ---------- Migração de plano ----------

    @Test
    void migrarPlano_valeParaTodosOsUsuariosDaConta() throws Exception {
        JsonNode assinante = criarAssinante("Ana", "ana@email.com", "BASICO");
        long assinanteId = assinante.get("id").asLong();
        long contaId = assinante.get("contaId").asLong();
        long criancaId = lerJson(criarNaoAssinante(contaId, assinanteId, "Crianca", "crianca@email.com", "INFANTIL")
                .andReturn()).get("id").asLong();

        // No plano BASICO a criança vê 2 filmes (o "Filme Premium" fica de fora)
        mockMvc.perform(get("/filmes").header(CABECALHO, criancaId))
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(put("/contas/{id}/plano", contaId)
                        .header(CABECALHO, assinanteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"plano\":\"PREMIUM\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plano").value("PREMIUM"));

        // Depois da migração a criança também enxerga o filme premium
        mockMvc.perform(get("/filmes").header(CABECALHO, criancaId))
                .andExpect(jsonPath("$.length()").value(3));
        assistir(filmePremium, criancaId);
    }

    @Test
    void migrarPlano_porNaoAssinante_deveSerNegado() throws Exception {
        JsonNode assinante = criarAssinante("Ana", "ana@email.com", "BASICO");
        long assinanteId = assinante.get("id").asLong();
        long contaId = assinante.get("contaId").asLong();
        long filhoId = lerJson(criarNaoAssinante(contaId, assinanteId, "Filho", "filho@email.com", "ADOLESCENTE")
                .andReturn()).get("id").asLong();

        mockMvc.perform(put("/contas/{id}/plano", contaId)
                        .header(CABECALHO, filhoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"plano\":\"PREMIUM\"}"))
                .andExpect(status().isForbidden());
    }

    // ---------- Métodos auxiliares ----------

    private JsonNode criarAssinante(String nome, String email, String plano) throws Exception {
        String json = String.format(
                "{\"nome\":\"%s\",\"email\":\"%s\",\"tipo\":\"ASSINANTE\",\"plano\":\"%s\"}", nome, email, plano);

        MvcResult resultado = mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        return lerJson(resultado);
    }

    private ResultActions criarNaoAssinante(long contaId, long solicitanteId, String nome, String email,
            String perfil) throws Exception {
        String json = String.format(
                "{\"nome\":\"%s\",\"email\":\"%s\",\"tipo\":\"NAO_ASSINANTE\",\"perfil\":\"%s\",\"contaId\":%d}",
                nome, email, perfil, contaId);

        return mockMvc.perform(post("/usuarios")
                .header(CABECALHO, solicitanteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private void assistir(Filme filme, long usuarioId) throws Exception {
        mockMvc.perform(post("/filmes/{id}/assistir", filme.getId()).header(CABECALHO, usuarioId))
                .andExpect(status().isCreated());
    }

    private JsonNode lerJson(MvcResult resultado) throws Exception {
        return objectMapper.readTree(resultado.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }
}
