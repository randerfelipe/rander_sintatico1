package compiladores.reconhecedor;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App
{
	public static void main( String[] args )
	{

		String nomeArquivo = "teste.pas";

		substituirTabulacao(nomeArquivo);

		AnalisadorSintatico analisadorSintatico = new AnalisadorSintatico(nomeArquivo);

		analisadorSintatico.Analisar();

	}


	public static void substituirTabulacao(String nomeArquivo) {
		Path caminhoArquivo = Paths.get(nomeArquivo);
		int numeroEspacosPorTab = 4;
		StringBuilder juntando = new StringBuilder();
		String espacos;

		for (int cont = 0; cont < numeroEspacosPorTab; cont++) {
			juntando.append(" ");
		}
		espacos = juntando.toString();

		String conteudo;
		try {
			conteudo = new String(Files.readAllBytes(caminhoArquivo), StandardCharsets.UTF_8);
			conteudo = conteudo.replace("\t", espacos);
			Files.write(caminhoArquivo, conteudo.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
