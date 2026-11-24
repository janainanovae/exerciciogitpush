import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class CatalogoProdutos {

    // Busca um produto pelo nome. Devolve um Optional: o produto pode não existir,
    // e isso fica explícito na assinatura do método (em vez de devolver null).
    public static Optional<Produto> buscarPorNome(List<Produto> produtos, String nome) {
        return produtos.stream()
                .filter(p -> p.getNome().equalsIgnoreCase(nome))
                .findFirst();
    }

    // Trata o Optional com ifPresentOrElse: um caminho para "existe" e outro para "não existe"
    public static void exibirBusca(List<Produto> produtos, String nome) {
        buscarPorNome(produtos, nome).ifPresentOrElse(
                produto -> System.out.println("Produto encontrado: " + produto.getNome()
                        + " - " + String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", produto.getPreco())),
                () -> System.out.println("Produto não encontrado."));
    }

    public static void main(String[] args) {
        List<Produto> produtos = Arrays.asList(
                new Produto("Notebook", 3500.00),
                new Produto("Mouse", 79.90),
                new Produto("Teclado", 199.90),
                new Produto("Monitor", 899.00),
                new Produto("Cabo HDMI", 35.00),
                new Produto("Headset", 100.00));   // exatamente 100: não é "maior que 100"

        // 1) Nova lista só com os produtos de preço maior que 100
        List<Produto> caros = produtos.stream()
                .filter(p -> p.getPreco() > 100)
                .collect(Collectors.toList());

        System.out.println("===== Produtos com preço maior que R$ 100,00 =====");
        caros.forEach(System.out::println);

        // 2) Lista ordenada pelo nome, em ordem alfabética (sem diferenciar maiúsculas de minúsculas)
        List<Produto> ordenados = produtos.stream()
                .sorted(Comparator.comparing(Produto::getNome, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        System.out.println();
        System.out.println("===== Produtos ordenados por nome =====");
        ordenados.forEach(System.out::println);

        // 3) Busca com Optional: um nome que existe e outro que não existe
        System.out.println();
        System.out.println("===== Busca por nome =====");
        System.out.println("Buscando \"Teclado\":");
        exibirBusca(produtos, "Teclado");

        System.out.println("Buscando \"Webcam\":");
        exibirBusca(produtos, "Webcam");
    }
}
