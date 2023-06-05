package compiladores.reconhecedor;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

public class Lexico {

	private char caractere;
	private StringBuilder lexema;
	private BufferedReader br;
	private String nomeArquivo;
	private int linha = 1;
	private int coluna = 1;
	private int tamanho = 0;
	private TabelaSimbolos tabSim;
	private Map<String, String> tabPalRes;

//	public Token qualProximoToken() {
//        Token t = null;
//        try {
//            br.mark(1);
//            t = getToken();
//            br.reset();
//        } catch (IOException e) {
//            System.err.println("Erro descobrindo qual o próximo token.");
//            e.printStackTrace();
//        }
//        return t;
//    }

	public Lexico(String nomeArquivo, BufferedReader br, TabelaSimbolos tabSim, Map<String, String> tabPalRes) {
		this.nomeArquivo = nomeArquivo;
		this.br = br;
		this.tabSim = tabSim;
		this.tabPalRes = tabPalRes;
		try {
			caractere = (char) br.read();
		} catch (IOException ex) {
			System.err.println("Não foi possível ler do arquivo: " + nomeArquivo);
			ex.printStackTrace();
		}
	}

	public Token getToken() {
		Token t = null;
		try {
			while ((int) caractere != 65535) { // EOF
				lexema = new StringBuilder();
				if (caractere <= 32 && caractere != -1) {
					while (caractere <= 32 && caractere != -1) {
						if (caractere == '\n') {
							linha++;
							coluna = 0;
							tamanho = 0;
						}
						caractere = (char) br.read();
						coluna++;

					}
				} else if (Character.isDigit(caractere)) {
					return numero();
				} else if (Character.isLetter(caractere)) {
					return identificador();
				} else if (caractere == ':') {
					caractere = (char) br.read();
					coluna++;
					t = new Token(linha, coluna, Classe.cDoisPontos);
					tamanho = 1;
					t.setTamanhoLexema(tamanho);
					if (caractere == '=') {
						tamanho = 2;
						t.setTamanhoLexema(tamanho);
						t.setClasse(Classe.cAtribuicao);
						caractere = (char) br.read();
						coluna++;
					}
					return t;
				} else if (caractere == '<') {
					caractere = (char) br.read();
					coluna++;
					t = new Token(linha, coluna, Classe.cMenor);
					tamanho = 1;
					t.setTamanhoLexema(tamanho);
					if (caractere == '=') {
						tamanho = 2;
						t.setTamanhoLexema(tamanho);
						t.setClasse(Classe.cMenorIgual);
						caractere = (char) br.read();
						coluna++;
					} else if (caractere == '>') {
						tamanho = 2;
						t.setTamanhoLexema(tamanho);
						t.setClasse(Classe.cDiferente);
						caractere = (char) br.read();
						coluna++;
					}
					return t;
				} else if (caractere == '>') {
					caractere = (char) br.read();
					coluna++;
					t = new Token(linha, coluna, Classe.cMaior);
					tamanho = 1;
					t.setTamanhoLexema(tamanho);
					if (caractere == '=') {
						tamanho = 2;
						t.setTamanhoLexema(tamanho);
						t.setClasse(Classe.cMaiorIgual);
						caractere = (char) br.read();
						coluna++;
					}
					return t;
				} else if (caractere == '+') {
					caractere = (char) br.read();
					coluna++;
					t = new Token(linha, coluna, Classe.cSoma);
					tamanho = 1;
					t.setTamanhoLexema(tamanho);
					return t;
				} else if (caractere == '-') {
					caractere = (char) br.read();
					coluna++;
					t = new Token(linha, coluna, Classe.cSub);
					tamanho = 1;
					t.setTamanhoLexema(tamanho);
					return t;
				} else if (caractere == '*') {
					caractere = (char) br.read();
					coluna++;
					t = new Token(linha, coluna, Classe.cMult);
					tamanho = 1;
					t.setTamanhoLexema(tamanho);
					return t;
				} else if (caractere == '/') {
					caractere = (char) br.read();
					coluna++;
					t = new Token(linha, coluna, Classe.cDiv);
					tamanho = 1;
					t.setTamanhoLexema(tamanho);
					return t;
				} else if (caractere == '(') {
					caractere = (char) br.read();
					coluna++;
					t = new Token(linha, coluna, Classe.cParenteseEsq);
					tamanho = 1;
					t.setTamanhoLexema(tamanho);
					return t;
				} else if (caractere == ')') {
					caractere = (char) br.read();
					coluna++;
					t = new Token(linha, coluna, Classe.cParenteseDir);
					tamanho = 1;
					t.setTamanhoLexema(tamanho);
					return t;
				} else if (caractere == ',') {
					caractere = (char) br.read();
					coluna++;
					t = new Token(linha, coluna, Classe.cVirgula);
					tamanho = 1;
					t.setTamanhoLexema(tamanho);
					return t;
				} else if (caractere == ';') {
					caractere = (char) br.read();
					coluna++;
					t = new Token(linha, coluna, Classe.cPtVirgula);
					tamanho = 1;
					t.setTamanhoLexema(tamanho);
					return t;
				} else if (caractere == '.') {
					caractere = (char) br.read();
					coluna++;
					t = new Token(linha, coluna, Classe.cPonto);
					tamanho = 1;
					t.setTamanhoLexema(tamanho);
					return t;
				} else if (caractere == '=') {
					caractere = (char) br.read();
					coluna++;
					t = new Token(linha, coluna, Classe.cIgual);
					tamanho = 1;
					t.setTamanhoLexema(tamanho);
					return t;
				} else if (caractere == '{') { // ignora comentario
					while (caractere != '}') {
						if ((int) caractere == 65535) {
							return new Token(linha, coluna, Classe.cEOF);
						} else if (caractere == '\n') {
							linha++;
							coluna = 0;
						}
						caractere = (char) br.read();
						t = new Token(linha, coluna);
						coluna++;
//						System.out.println((int) caractere);
					}
					caractere = (char) br.read();
				} else if ((int) caractere != 65535) {
					System.err.println("Ocorreu um Erro Léxico: Caractere não reconhecido - " + caractere + " - "
							+ ((int) caractere) + " , coluna: " + coluna + " , linha: " + linha);
					caractere = (char) br.read();
					coluna++;
				}
			}
			t = new Token(linha, coluna, Classe.cEOF);
		} catch (IOException ex) {
			System.err.println("Não foi possível ler do arquivo: " + nomeArquivo);
			ex.printStackTrace();
		}
		return t;
	}

	private Token numero() {
		tamanho = 0;
		Token t = null;
		try {
			if (Character.isDigit(caractere)) {
				lexema = new StringBuilder();
				t = new Token(linha, coluna, Classe.cInt);
				while (Character.isDigit(caractere)) {
					lexema.append(caractere);
					caractere = (char) br.read();
					coluna++;
					tamanho++;
				}
				Valor valor = new Valor();
				valor.setInteiro(Integer.parseInt(lexema.toString()));
				t.setValor(valor);
				if (caractere == '.') {
					tamanho++;
					lexema.append(caractere);
					caractere = (char) br.read();
					coluna++;
					if (Character.isDigit(caractere)) {
						while (Character.isDigit(caractere)) {
							lexema.append(caractere);
							caractere = (char) br.read();
							tamanho++;
						}
					} else {
						System.err.println("Ocorreu um Erro Léxico: Falta parte decimal no número");
					}
					t.setClasse(Classe.cReal);
					valor.setDecimal(Double.parseDouble(lexema.toString()));
					t.setValor(valor);
				}
				t.setTamanhoLexema(tamanho);
				tamanho = 0;
			}
		} catch (IOException ex) {
			System.err.println("Não foi possível ler do arquivo: " + nomeArquivo);
			ex.printStackTrace();
		}
		return t;
	}

	private Token identificador() {
		tamanho = 0;
		Token t = null;
		try {
			if (Character.isLetter(caractere)) {
				lexema = new StringBuilder();
				t = new Token(linha, coluna);
				while (Character.isLetter(caractere) || Character.isDigit(caractere)) {
					lexema.append(caractere);
					caractere = (char) br.read();
					coluna++;
					tamanho++;
				}
				if (tabPalRes.containsKey(lexema.toString().toLowerCase())) {
					t.setClasse(Classe.cPalRes);
					Valor valor = new Valor();
					valor.setIdentificador(lexema.toString().toLowerCase());
					t.setValor(valor);
				} else {
					t.setClasse(Classe.cId);
					if (!tabSim.isPresent(lexema.toString())) {
						tabSim.add(lexema.toString());
						Valor valor = new Valor();
						valor.setIdentificador(lexema.toString());
						t.setValor(valor);
					}
				}

				t.setTamanhoLexema(tamanho);
				tamanho = 0;
			}
		} catch (IOException ex) {
			System.err.println("Não foi possível ler do arquivo: " + nomeArquivo);
			ex.printStackTrace();
		}
		return t;
	}

}

