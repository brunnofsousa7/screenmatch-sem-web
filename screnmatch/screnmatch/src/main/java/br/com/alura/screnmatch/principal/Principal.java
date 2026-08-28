package br.com.alura.screnmatch.principal;

import br.com.alura.screnmatch.model.*;
import br.com.alura.screnmatch.repository.SerieRepository;
import br.com.alura.screnmatch.service.ConsumoApi;
import br.com.alura.screnmatch.service.ConverteDados;

import java.util.*;

public class Principal {

    private SerieRepository repositorio;

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();

    private final String ENDERECO= "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=" + System.getenv("OMDB_APIKEY");
    private List<Serie> series = new ArrayList<>();
    public Principal(SerieRepository repositorio) {
        this.repositorio = repositorio;
    }
    private Optional<Serie> serieBusca;

    public void exibeMenu() {
        var opcao = -1;
        while (true) {
            var menu = """
                1 - Buscar série
                2 - Buscar episodio
                3 - Busacar lista de Séries
                4 - Buscar série pelo Título
                5 - Buscar série pelo nome do Ator 
                6 - Buscar top 5 séries
                7 - Buscar série por categoria
                8 - Buscar séire por total de teporadas
                9 - Buscar episósio por trecho
                10 - Buscar Top episodios 5 por série
                11 - Buscar episódio a partir de uma data
                0 - sair
                """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();
            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePeloTitulo();
                    break;
                case 5:
                    buscarSeriePeloNomeDoAtor();
                    break;
                case 6:
                    buscarTop5Serie();
                    break;
                case 7:
                    buscarSeriePorCategoria();
                    break;
                case 8:
                    buscarPorTotalTemporadas();
                    break;
                case 9:
                    buscarEpisodioPorTrecho();
                    break;
                case 10:
                    buscarTopEpisodiosSerie();
                    break;
                case 11:
                    buscarEpisodioDepoisDeUmaData();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println();
            }

        }

    }

    private void buscarSerieWeb(){
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
        repositorio.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie(){
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        if (dados.totalTemporada() == null){
            System.out.println("A série encontrada não possui informação de temporadas.");
        }
        return dados;
    }

    private void buscarEpisodioPorSerie(){
        listarSeriesBuscadas();
        System.out.println("Escolha uma série pelo nome: ");
        var serieNome = leitura.nextLine();

        Optional<Serie> serie = repositorio.findByTituloContainingIgnoreCase(serieNome);

        if (serie.isPresent()){
            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporada(); i++){
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .toList();

            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);
        } else{
            System.out.println("Série não encontrada!");
        }

    }

    private void listarSeriesBuscadas(){

        series = repositorio.findAll();
        series.stream()
                        .sorted(Comparator.comparing(Serie::getGenero))
                                .forEach(System.out::println);

    }

    private void buscarSeriePeloTitulo(){
        System.out.println("Escolha uma série pelo Título: ");
        var nomeSerie = leitura.nextLine();

        serieBusca = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBusca.isPresent()){
            System.out.println("Dados da série: " + serieBusca.get());
        }else {
            System.out.println("Série não encontrada");
        }

    }

    private void buscarSeriePeloNomeDoAtor(){
        System.out.println("Qual o nome para busca? ");
        var nomeAtor = leitura.nextLine();
        System.out.println("Avaliação a partir de qual valor? ");
        var avaliacao = leitura.nextDouble();

        List<Serie> buscarAtor = repositorio.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacao);
        System.out.println("Série em que " + nomeAtor + " trabalhou: ");
        buscarAtor.forEach(s ->
                System.out.println(s.getTitulo() + " avaliação: " + s.getAvaliacao()));

    }

    private void buscarTop5Serie(){
        List<Serie> serieTop = repositorio.findTop5ByOrderByAvaliacaoDesc();
        System.out.println("Top 5 séries: ");
            serieTop.forEach(s ->
                    System.out.println(s.getTitulo() + " avliação: " + s.getAvaliacao()));
    }

    private void buscarSeriePorCategoria(){
        System.out.println("Deseja buscara série por categora/gênero? ");
        var nomeGenero = leitura.nextLine();
        Categoria categoria = Categoria.fromPortugues(nomeGenero);
        List<Serie> seriePorCategoria = repositorio.findByGenero(categoria);
        System.out.println("Genero da série: " + nomeGenero);
        seriePorCategoria.forEach(System.out::println);
    }

    private void buscarPorTotalTemporadas(){
        System.out.println("Deseja busca a série até quantas temporada? ");
        var temporadas = leitura.nextInt();
        System.out.println("Avaliação a partir de qual valor? ");
        var avaliacao = leitura.nextDouble();
        List<Serie> serieTotalTemporadas = repositorio.seriePorTemporadasEAvaliacao(temporadas,avaliacao);
        System.out.println("Series com até " + temporadas + " temporadas");
        serieTotalTemporadas.forEach(s ->
                System.out.println(s.getTitulo() + " avaliação " + s.getAvaliacao()));
    }

    private void buscarEpisodioPorTrecho(){
        System.out.println("Qual o nome do episódio para busca? ");
        var trechoEpisodio = leitura.nextLine();
        List<Episodio> episodiosEcontrados = repositorio.episodioPorTrecho(trechoEpisodio);
        episodiosEcontrados.forEach(e ->
                System.out.printf("Série: %s Temporada: %s - Episodio %s - %s\n",
                        e.getSerie().getTitulo(), e.getTemporada(),
                        e.getNumeroEpisodio(), e.getTitulo()));
    }

    private void buscarTopEpisodiosSerie(){
        buscarSeriePeloTitulo();
        if (serieBusca.isPresent()){
            Serie serie = serieBusca.get();
            List<Episodio> topEpisodio = repositorio.buscarTopEspisodios(serie);
            topEpisodio.forEach(e ->
                    System.out.printf("Série: %s Temporada: %s - Episodio %s - %s - Avaliação: %s\n",
                            e.getSerie().getTitulo(), e.getTemporada(),
                            e.getNumeroEpisodio(), e.getTitulo(), e.getAvaliacao()));
        }

    }

    private void buscarEpisodioDepoisDeUmaData(){
        buscarSeriePeloTitulo();
        if (serieBusca.isPresent()){
            Serie serie = serieBusca.get();
            System.out.println("Digite o ano limite de lançamento: ");
            var anoLancamento = leitura.nextInt();
            leitura.nextLine();
            List<Episodio> episodioAno = repositorio.episodioPorSerieEAno(serie,anoLancamento);
            episodioAno.forEach(System.out::println);
        }
    }



}



