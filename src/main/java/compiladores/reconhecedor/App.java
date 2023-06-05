package compiladores.reconhecedor;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Hello world!
 *
 */
public class App {

	public static void main(String[] args) {
		String nomeArquivo = "teste.pas";
		substituirTabulacao(nomeArquivo);
		Compilador c = new Compilador(nomeArquivo);
		c.sintatico();
		
	}

	public static void substituirTabulacao(String nomeArquivo) {
		Path caminhoArquivo = Paths.get(nomeArquivo);
		int numeroEspacosPorTab = 4;
		StringBuilder juntando = new StringBuilder();

		for(int cont = 0; cont < numeroEspacosPorTab; ++cont) {
			juntando.append(" ");
		}

		String espacos = juntando.toString();

		try {
			String conteudo = new String(Files.readAllBytes(caminhoArquivo), StandardCharsets.UTF_8);
			conteudo = conteudo.replace("\t", espacos);
			Files.write(caminhoArquivo, conteudo.getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
		} catch (IOException var7) {
			var7.printStackTrace();
		}

	}
}
	
