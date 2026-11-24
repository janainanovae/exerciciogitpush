import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ConsultasStream {

    private static final int ANO_INICIAL = 1975;
    private static final int ANO_FINAL = 1985;
    private static final double SALARIO_MAXIMO = 3000.00;
    private static final int IDADE_LIMITE = 25;
    private static final int QUANTIDADE_RANKING = 30;

    // 1) Funcionários de qualquer empresa nascidos entre 1975 e 1985 com salário <= R$ 3.000,00
    public static List<Funcionario> filtrarPorNascimentoESalario(List<Empresa> empresas) {
        return empresas.stream()
                // flatMap junta os funcionários de todas as empresas em um único fluxo
                .flatMap(empresa -> empresa.getFuncionarios().stream())
                .filter(f -> f.getDataNascimento().getYear() >= ANO_INICIAL
                        && f.getDataNascimento().getYear() <= ANO_FINAL)
                .filter(f -> f.getSalario() <= SALARIO_MAXIMO)
                .collect(Collectors.toList());
    }

    // 2) Soma dos números pares da lista
    public static int somarPares(List<Integer> numeros) {
        return numeros.stream()
                .filter(numero -> numero % 2 == 0)
                .mapToInt(Integer::intValue)
                .sum();
    }

    // 3) Os 30 maiores salários de funcionários CLT com menos de 25 anos
    public static List<Double> maioresSalariosCltMenoresDe25(List<Empresa> empresas) {
        return empresas.stream()
                .flatMap(empresa -> empresa.getFuncionarios().stream())
                .filter(f -> f.getTipoContrato() == TipoContrato.CLT)
                .filter(f -> f.getIdade() < IDADE_LIMITE)
                .map(Funcionario::getSalario)
                .sorted(Comparator.reverseOrder())
                .limit(QUANTIDADE_RANKING)
                .collect(Collectors.toList());
    }
}
