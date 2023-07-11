package compiladores.reconhecedor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class AnalisadorSintatico {

	//VARIAVEIS NOVAS

	private TabelaSimbolos tabela;

	private int endereco;
	private int contRotulo = 1;

	private List<Registro> ultimasVariaveisDeclaradas = new ArrayList<>();

	private String nomeArquivoSaida;
	private String caminhoArquivoSaida;
	private BufferedWriter bw;
	private FileWriter fw;
	private String rotulo = "";
	private String rotElse;
	private char caractere;
	private StringBuilder lexema;
	private BufferedReader br;
	private String nomeArquivo;
	private int linha = 1;
	private int coluna = 1;
	private int tamanho = 0;
	private TabelaSimbolos tabSim;
	private Map<String, String> tabPalRes;

	private AnalisadorLexico lexico;
	private Token token;

	public void LerToken(){
		token = lexico.getToken(linha, coluna);
		coluna = token.getColuna()+token.getTamanhoToken();
		linha = token.getLinha();
		System.out.println(token);

	}

	public AnalisadorSintatico(String nomeArquivo){
		linha=1;
		coluna=1;
		this.lexico=new AnalisadorLexico(nomeArquivo);
	}

	public void Analisar(){
		LerToken();

		this.endereco = 0;

		nomeArquivoSaida = "arquivogerado.c";
		caminhoArquivoSaida = Paths.get(nomeArquivoSaida).toAbsolutePath().toString();

		bw = null;
		fw = null;

		try {
			fw = new FileWriter(caminhoArquivoSaida, Charset.forName("UTF-8"));
			bw = new BufferedWriter(fw);
			programa();
			bw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(this.tabela);

	}
	private String criarRotulo(String texto) {
		String retorno = "rotulo" + texto + contRotulo;
		contRotulo++;
		return retorno;
	}

	private void gerarCodigo(String instrucoes) {
		try {
			if (rotulo.isEmpty()) {
				bw.write(instrucoes + "\n");
			} else {
				bw.write(rotulo + ": " +  instrucoes + "\n");
				rotulo = "";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void mensagemErro(String msg) {
		System.err.println("Linha: " + token.getLinha() +
				", Coluna: " + token.getColuna() +
				msg);
	}

	public void programa() {
		if ((token.getClasse() == Classe.cPalRes)
				&& (token.getValor().getValorIdentificador().equalsIgnoreCase("program"))) {
			LerToken();
			if (token.getClasse() == Classe.cId) {
				LerToken();
				action1();
				corpo();
				if (token.getClasse() == Classe.cPonto) {
					LerToken();
				} else {
					mensagemErro(" O programa não foi encerrado, pois não foi encontrado o '.'");
				}
				action2();
			} else {
				mensagemErro(" Não foi encontrado o nome");
			}
		} else {
			mensagemErro("Erro de inicialização no comando Begin");
		}
	}

	public void action1()
	{
		tabela=new TabelaSimbolos();

		tabela.setTabelaPai(null);

		Registro registro=new Registro();
		registro.setNome(token.getValor().getValorIdentificador());
		registro.setCategoria(Categoria.PROGRAMAPRINCIPAL);

		registro.setNivel(0);
		registro.setOffset(0);
		registro.setTabelaSimbolos(tabela);
		registro.setRotulo("main");
		tabela.inserirRegistro(registro);
		String codigo = "#include <stdio.h>\n" +
				"\nint main(){\n";
		gerarCodigo(codigo);
	}

	public void action2()
	{
		Registro registro=new Registro();
		registro.setNome(null);
		registro.setCategoria(Categoria.PROGRAMAPRINCIPAL);
		registro.setNivel(0);
		registro.setOffset(0);
		registro.setTabelaSimbolos(tabela);
		registro.setRotulo("finalCode");
		tabela.inserirRegistro(registro);
		String codigo = "\n}\n";
		gerarCodigo(codigo);
	}

	public void corpo() {
		declara();
		if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("begin"))) {
			LerToken();
			sentencas();
			if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("end"))) {
				LerToken();
			}else {
				mensagemErro(" Erro de finalização End");
			}
		}else {
			mensagemErro("Erro de inicialização Begin");
		}
	}

	public void declara() {
		if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("var"))) {
			LerToken();
			dvar();
			mais_dc();
		}
	}

	public void mais_dc() {
		if (token.getClasse() == Classe.cPontoVirgula) {
			LerToken();
			cont_dc();
		} else {
			mensagemErro(" Não encontrado ';' ");
		}
	}

	public void cont_dc() {
		if (token.getClasse() == Classe.cId) {
			dvar();
			mais_dc();
		}
	}

	public void dvar() {
		variaveis();
		if (token.getClasse() == Classe.cDoisPontos) {
			LerToken();
			tipo_var();
		}else {
			mensagemErro("Não foi encontrado o ':'");
		}
	}

	public void tipo_var() {
		if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("integer"))) {
			action3("int");
			LerToken();


		}else if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("real"))) {
			action3("float");
			LerToken();
		}else {
			mensagemErro(" Não encontrada declaração do tipo ");
		}

	}

	private void action3(String type) {
		String codigo= '\t'+type;
		for(int i=0;i<this.ultimasVariaveisDeclaradas.size();i++)
		{
			codigo=codigo+' '+ this.ultimasVariaveisDeclaradas.get(i).getNome();
			if(i == this.ultimasVariaveisDeclaradas.size()-1)
			{
				codigo=codigo + ';';
			}
			else{
				codigo=codigo + ',';
			}
		}
		gerarCodigo(codigo);
	}

	public void action4()
	{
		Registro registro=new Registro();
		registro.setNome(token.getValor().getValorIdentificador());
		registro.setCategoria(Categoria.VARIAVEL);
		registro.setNivel(0);
		registro.setOffset(0);
		registro.setTabelaSimbolos(tabela);
		this.endereco++;
		registro.setRotulo("variavel"+this.endereco);
		ultimasVariaveisDeclaradas.add(registro);
		this.tabela.inserirRegistro(registro);
	}

	public void variaveis() {
		if (token.getClasse() == Classe.cId) {
			action4();
			LerToken();
			mais_var();
		}else {
			mensagemErro(" Identificador não encontrado");
		}
	}

	public void mais_var(){
		if (token.getClasse() == Classe.cVirgula) {
			LerToken();
			variaveis();
		}
	}

	public void sentencas() {
		comando();
		mais_sentencas();
	}

	public void mais_sentencas() {
		if (token.getClasse() == Classe.cPontoVirgula) {
			LerToken();
			cont_sentencas();
		}else {
			mensagemErro(" ; não encontrado");
		}
	}

	public void cont_sentencas() {
		if (((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("read"))) ||
				((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("write"))) ||
				((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("for"))) ||
				((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("repeat"))) ||
				((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("while"))) ||
				((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("if"))) ||
				((token.getClasse() == Classe.cId))
		) {
			sentencas();
		}
	}

	public List<Token> var_read(List<Token> listaTokenLidos) {
		if (token.getClasse() == Classe.cId) {
			listaTokenLidos.add(token);
			LerToken();
			listaTokenLidos = mais_var_read(listaTokenLidos);
		}else {
			mensagemErro(" ID não encontrado");
		}
		return listaTokenLidos;
	}

	public List<Token> mais_var_read(List<Token> listaTokenLidos) {
		if (token.getClasse() == Classe.cVirgula) {
			LerToken();
			listaTokenLidos = var_read(listaTokenLidos);
		}
		return listaTokenLidos;
	}

	public String var_write(String codigo) {

		if (token.getClasse() == Classe.cId) {
			codigo=codigo+token.getValor().getValorIdentificador();
			LerToken();
			codigo=mais_var_write(codigo);
		}else {
			mensagemErro(" ID não encontrado");
		}

		return codigo;
	}

	public String mais_var_write(String codigo) {
		if (token.getClasse() == Classe.cVirgula) {
			codigo=codigo+ ',';
			LerToken();
			codigo=var_write(codigo);

		}
		return codigo;
	}

	public void comando() {

		if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("read"))){
			String codigo="\tscanf";
			LerToken();
			if (token.getClasse() == Classe.cParEsq) {
				codigo=codigo+"(\"";
				LerToken();
				List<Token> arrayToken = new ArrayList<Token>();
				arrayToken=var_read(arrayToken);
				for(Token i: arrayToken){
					codigo=codigo+"%d ";
				}
				codigo=codigo+"\", ";
				for(Token i: arrayToken){
					if(i == arrayToken.get(arrayToken.size()-1)){
						codigo=codigo+"&"+i.getValor().getValorIdentificador();
					}else{
						codigo=codigo+"&"+i.getValor().getValorIdentificador()+", ";
					}
				}
				if (token.getClasse() == Classe.cParDir) {

					codigo=codigo+");";
					gerarCodigo(codigo);
					LerToken();
				}else {
					mensagemErro(" ')' Não foi encontrado");
				}
			}else {
				mensagemErro(" '(' Não foi encontrado");
			}
		}else
			if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("write"))){
				String referencias="\tprintf";
				String codigo = "";
				LerToken();
				if (token.getClasse() == Classe.cParEsq) {
					referencias = referencias + "(\"";
					LerToken();

					codigo=codigo+var_write("");

					if (codigo.length() >  0) {
						referencias = referencias + "%d ".repeat(codigo.split(",").length);
						referencias = referencias + "\", ";
					} else {
						referencias = referencias + "\"";
					}

					if (token.getClasse() == Classe.cParDir) {
						codigo=codigo+");";
						gerarCodigo(referencias + codigo);
						LerToken();
					}else {
						mensagemErro("')' Não foi encontrado");
					}
				}else {
					mensagemErro("'(' Não foi encontrado");
				}
			}else

			if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("for"))){
				String codigo="\n\tfor(";
				LerToken();
				if (token.getClasse() == Classe.cId) {
					String identificador = token.getValor().getValorIdentificador();
					codigo=codigo+identificador;
					LerToken();

					if (token.getClasse() == Classe.cAtribuicao){
						codigo=codigo+"=";
						LerToken();

						codigo=codigo+expressao();

						if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("to"))){
							codigo=codigo+";";
							LerToken();
							codigo=codigo+identificador;
							codigo=codigo+"<="+expressao()+";";
							codigo=codigo+identificador + "++)";
							if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("do"))){
								LerToken();
								if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("begin"))){
									codigo=codigo+"{";
									gerarCodigo(codigo);
									LerToken();
									sentencas();
									if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("end"))){
										String codigoFinal = "\t}";
										gerarCodigo(codigoFinal);
										LerToken();
									}else {
										mensagemErro(" Erro na finalização do laço");
									}
								}else {
									mensagemErro("Erro na inicialização do Laço");
								}
							}else {
								mensagemErro(" Não foi encontrado a função Do no Laço");
							}
						}else {
							mensagemErro(" Não foi encontrado a função To no Laço");
						}
					}else {
						mensagemErro("Não foi encontrado a função := no Laço");
					}
				}else {
					mensagemErro(" Não foi encontrado a ID no Laço");
				}
			}else

			if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("repeat"))){
				String codigo="\n\tdo {\n\t";

				LerToken();
				gerarCodigo(codigo);
				sentencas();
				if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("until"))){

					LerToken();
					if (token.getClasse() == Classe.cParEsq){
						String codigoFinal="\n\t}while";
						codigoFinal=codigoFinal+"(";
						LerToken();

						codigoFinal=codigoFinal+condicao();

						if (token.getClasse() == Classe.cParDir){
							codigoFinal=codigoFinal+");";
							gerarCodigo(codigoFinal);
							LerToken();

						}else {
							mensagemErro(" Não encontrado ) no repeat");
						}
					}else {
						mensagemErro("  Não encontrado ( no repeat");
					}
				}else {
					mensagemErro("Não encontrado Util no repeat");
				}
			}

			else if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("while"))){
				String codigo="\n\twhile";
				LerToken();
				if (token.getClasse() == Classe.cParEsq){
					codigo=codigo+"(";
					LerToken();
					codigo=codigo+condicao();
					if (token.getClasse() == Classe.cParDir){
						codigo=codigo+")";
						LerToken();
						if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("do"))){
							LerToken();
							if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("begin"))){
								codigo=codigo+"{\n";
								gerarCodigo(codigo);
								LerToken();
								sentencas();
								if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("end"))){
									codigo="\t}\n";
									gerarCodigo(codigo);
									LerToken();
								}else {
									mensagemErro(" Não encontrado END no While");
								}
							}else {
								mensagemErro(" Não encontrado Begin no While");
							}
						}else {
							mensagemErro(" Não encontrado DO no While");
						}
					}else {
						mensagemErro(" Não encontrado ) no While");
					}
				}else {
					mensagemErro(" Não encontrado ( no While");
				}
			}
			else if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("if"))){
				String codigo="";
				LerToken();
				if (token.getClasse() == Classe.cParEsq){
					codigo=codigo+"\n\tif(";
					LerToken();
					codigo=codigo+condicao();
					if (token.getClasse() == Classe.cParDir){
						codigo=codigo+")";
						LerToken();
						if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("then"))){
							LerToken();
							if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("begin"))){
								codigo=codigo +" {";
								gerarCodigo(codigo);
								LerToken();
								sentencas();
								if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("end"))){
									LerToken();

									String codigoFinal = "";
									codigoFinal = codigoFinal + "\t}";
									gerarCodigo(codigoFinal);
									pfalsa();
								}else {
									mensagemErro("Não encontrado END no While");
								}
							}else {
								mensagemErro(" Não encontrado Begin no While");
							}
						}else {
							mensagemErro("Não encontrado DO no While");
						}
					}else {
						mensagemErro("Não encontrado ) no While");
					}
				}else {
					mensagemErro("Não encontrado ( no While");
				}
			}
			else if (token.getClasse() == Classe.cId){
				String codigo="\n\t";
				codigo=codigo+token.getValor().getValorIdentificador();
				LerToken();
				if (token.getClasse() == Classe.cAtribuicao){
					codigo=codigo+"=";
					LerToken();
					codigo=codigo+expressao()+";";
					gerarCodigo(codigo);
				}
				else {
					mensagemErro("A atribuicao nao foi encontrada");
				}
			}
	}

	public String condicao() {
		String expressao1 = expressao();
		String relacao = relacao();
		String expressao2 = expressao();
		return expressao1 + relacao + expressao2;
	}

	public void pfalsa() {
		String codigo = "";
		if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("else"))){
			codigo = codigo + "\telse";
			LerToken();
			if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("begin"))){
				codigo = codigo + "{";
				gerarCodigo(codigo);
				LerToken();
				sentencas();
				if ((token.getClasse() == Classe.cPalRes) && (token.getValor().getValorIdentificador().equalsIgnoreCase("end"))){
					String codigoFinal = "\n\t}";
					gerarCodigo(codigoFinal);
					LerToken();
				}else {
					mensagemErro(" Comando End não encontrado");
				}
			}else {
				mensagemErro(" Comando Begin não encontrado");
			}
		}
	}

	public String relacao() {
		String operador="";
		if (token.getClasse() == Classe.cIgual) {
			operador="=";
			LerToken();
		}else if (token.getClasse() == Classe.cMaior) {
			operador=">";
			LerToken();
		}else if (token.getClasse() == Classe.cMenor) {
			operador="<";
			LerToken();
		}else if (token.getClasse() == Classe.cMaiorIgual) {
			operador = ">=";
			LerToken();
		}else if (token.getClasse() == Classe.cMenorIgual) {
			operador = "<=";
			LerToken();
		}else if (token.getClasse() == Classe.cDiferente) {
			operador = "!=";
			LerToken();
		}else {
			mensagemErro(" Operador matemático não encontrado");
		}

		return operador;
	}

	public String expressao() {
		String termo = termo();
		String outrosTermos = outros_termos();

		return termo + outrosTermos;
	}

	public String outros_termos() {
		String op = "";
		String termo= "";
		String outrosTermos = "";

		if (token.getClasse() == Classe.cMais || token.getClasse() == Classe.cMenos) {
			op = op_ad();
			termo = termo();
			outrosTermos = outros_termos();
		}

		return op + termo + outrosTermos;
	}

	public String op_ad() {
		String op = "";
		if (token.getClasse() == Classe.cMais) {
			op = "+";
			LerToken();
		} else if (token.getClasse() == Classe.cMenos) {
			op = "-";
			LerToken();
		}else {
			mensagemErro("  + e/ou - Não encontrados");
		}
		return op;
	}

	public String termo() {
		String fator = fator();
		String maisFatores = mais_fatores();

		return fator + maisFatores;
	}

	public String mais_fatores() {
		if (token.getClasse() == Classe.cMultiplicacao || token.getClasse() == Classe.cDivisao) {
			String op = op_mul();
			String fator = fator();
			String outrosFatores = mais_fatores();

			return op + fator + outrosFatores;
		}
		return "";
	}

	public String op_mul() {
		String op = "";
		if (token.getClasse() == Classe.cMultiplicacao) {
			op = "*";
			LerToken();
		} else if (token.getClasse() == Classe.cDivisao) {
			op = "/";
			LerToken();
		} else {
			mensagemErro(" * e/ou / não foram encontrados");
		}

		return op;
	}

	public String fator() {
		String validaFator = "";
		if (token.getClasse() == Classe.cId) {
			validaFator = token.getValor().getValorIdentificador();

			LerToken();
		} else if (token.getClasse() == Classe.cInt) {
			validaFator = String.valueOf(token.getValor().getValorInteiro());
			LerToken();
		} else if (token.getClasse() == Classe.cReal) {
			validaFator = String.valueOf(token.getValor().getValorDecimal());
			LerToken();
		}else if (token.getClasse() == Classe.cParEsq){
			validaFator="(";
			LerToken();
			validaFator = validaFator + expressao();
			if (token.getClasse() == Classe.cParDir){
				validaFator=validaFator + ")";
				LerToken();
			}else {
				mensagemErro(") não encontrado");
			}
		}else {
			mensagemErro(" (ID, Num, Exp) não encontrados");
		}

		return validaFator;
	}

}

