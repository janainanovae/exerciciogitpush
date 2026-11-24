import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TesteStreams {

    public static void main(String[] args) {
        testarFiltroPorNascimentoESalario();
        System.out.println();
        testarSomaDePares();
        System.out.println();
        testarMaioresSalarios();
    }

    // Massa de dados com casos dentro e fora do filtro, incluindo os limites
    private static void testarFiltroPorNascimentoESalario() {
        Empresa tech = new Empresa("Tech Ltda");
        tech.adicionarFuncionario(new Funcionario("Ana", LocalDate.of(1980, 6, 15), 2500.00, TipoContrato.CLT));
        tech.adicionarFuncionario(new Funcionario("Bruno", LocalDate.of(1975, 1, 1), 3000.00, TipoContrato.CLT));     // limite: entra
        tech.adicionarFuncionario(new Funcionario("Carla", LocalDate.of(1985, 12, 31), 3000.01, TipoContrato.CLT));   // salário acima
        tech.adicionarFuncionario(new Funcionario("Diego", LocalDate.of(1974, 12, 31), 1000.00, TipoContrato.CLT));   // nasceu antes de 1975

        Empresa comercial = new Empresa("Comercial SA");
        comercial.adicionarFuncionario(new Funcionario("Elisa", LocalDate.of(1985, 3, 20), 2800.00, TipoContrato.PJ));
        comercial.adicionarFuncionario(new Funcionario("Fábio", LocalDate.of(1986, 1, 1), 2000.00, TipoContrato.CLT)); // nasceu depois de 1985
        comercial.adicionarFuncionario(new Funcionario("Gustavo", LocalDate.of(1978, 10, 10), 8000.00, TipoContrato.CLT)); // salário alto

        List<Funcionario> resultado = ConsultasStream.filtrarPorNascimentoESalario(Arrays.asList(tech, comercial));

        System.out.println("===== 1) Nascidos entre 1975 e 1985 com salário <= R$ 3.000,00 =====");
        resultado.forEach(System.out::println);
        System.out.println("Esperado: Ana, Bruno e Elisa (" + resultado.size() + " encontrados)");
    }

    private static void testarSomaDePares() {
        System.out.println("===== 2) Soma dos números pares =====");
        verificar("[1..10]", ConsultasStream.somarPares(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)), 30);
        verificar("[-2, 3, 4]", ConsultasStream.somarPares(Arrays.asList(-2, 3, 4)), 2);
        verificar("[1, 3, 5] (sem pares)", ConsultasStream.somarPares(Arrays.asList(1, 3, 5)), 0);
        verificar("lista vazia", ConsultasStream.somarPares(new ArrayList<Integer>()), 0);
    }

    private static void verificar(String descricao, int resultado, int esperado) {
        String status = (resultado == esperado) ? "OK" : "ERRO";
        System.out.println(descricao + " -> " + resultado + " (esperado: " + esperado + ") [" + status + "]");
    }

    private static void testarMaioresSalarios() {
        LocalDate hoje = LocalDate.now();
        Empresa a = new Empresa("Empresa A");
        Empresa b = new Empresa("Empresa B");

        // 40 funcionários CLT com 20 anos, salários de 2000 a 5900, distribuídos entre duas empresas
        for (int i = 0; i < 40; i++) {
            Funcionario f = new Funcionario("Jovem " + i, hoje.minusYears(20), 2000.00 + i * 100, TipoContrato.CLT);
            if (i % 2 == 0) {
                a.adicionarFuncionario(f);
            } else {
                b.adicionarFuncionario(f);
            }
        }

        // Casos que devem ficar de fora, mesmo com salários altos
        a.adicionarFuncionario(new Funcionario("Já tem 25", hoje.minusYears(25), 99999.00, TipoContrato.CLT));   // 25 não é menor que 25
        a.adicionarFuncionario(new Funcionario("PJ jovem", hoje.minusYears(20), 50000.00, TipoContrato.PJ));     // não é CLT
        b.adicionarFuncionario(new Funcionario("Jovem top", hoje.minusYears(24), 10000.00, TipoContrato.CLT));   // entra, maior salário válido

        List<Double> ranking = ConsultasStream.maioresSalariosCltMenoresDe25(Arrays.asList(a, b));

        System.out.println("===== 3) 30 maiores salários de CLT com menos de 25 anos =====");
        System.out.println("Quantidade: " + ranking.size() + " (esperado: 30)");
        System.out.println("Maior salário: " + ranking.get(0) + " (esperado: 10000.0)");
        System.out.println("Menor salário do ranking: " + ranking.get(ranking.size() - 1) + " (esperado: 3100.0)");
        System.out.println("Ordem decrescente: " + estaEmOrdemDecrescente(ranking));
        System.out.println("Ranking: " + ranking);
    }

    private static boolean estaEmOrdemDecrescente(List<Double> valores) {
        List<Double> copia = new ArrayList<>(valores);
        copia.sort(Collections.reverseOrder());
        return copia.equals(valores);
    }
}
