import java.time.LocalDate;
import java.time.Period;
import java.util.Locale;

public class Funcionario {

    private String nome;
    private LocalDate dataNascimento;
    private double salario;
    private TipoContrato tipoContrato;

    public Funcionario(String nome, LocalDate dataNascimento, double salario, TipoContrato tipoContrato) {
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.salario = salario;
        this.tipoContrato = tipoContrato;
    }

    public String getNome() {
        return nome;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public double getSalario() {
        return salario;
    }

    public TipoContrato getTipoContrato() {
        return tipoContrato;
    }

    // A idade é calculada a partir da data de nascimento, então nunca fica desatualizada
    public int getIdade() {
        return Period.between(dataNascimento, LocalDate.now()).getYears();
    }

    @Override
    public String toString() {
        return String.format(Locale.forLanguageTag("pt-BR"),
                "%s | Nascimento: %s | Salário: R$ %.2f | Contrato: %s",
                nome, dataNascimento, salario, tipoContrato);
    }
}
