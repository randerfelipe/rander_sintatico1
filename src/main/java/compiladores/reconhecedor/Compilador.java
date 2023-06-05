package compiladores.reconhecedor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Compilador {
	
	private TabelaSimbolos tabSim = new TabelaSimbolos();
	private Lexico lexico;
	private Map<String, String> tabPalRes = new HashMap<String, String>();
	private Token t;

	public Compilador(String nomeArquivo) {
        try {
            String caminhoArquivo = Paths.get(nomeArquivo).toAbsolutePath().toString();
            FileReader fr = new FileReader(caminhoArquivo, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(fr);
            
            criarTabelaPalavrasReservadas();
            
            tabSim = new TabelaSimbolos();
            lexico = new Lexico(nomeArquivo, br, tabSim, tabPalRes);
        } catch (IOException ex) {
            System.err.println("Não foi possível abrir o arquivo ou ler do arquivo: " + nomeArquivo);
            ex.printStackTrace();
        }
    }

	public void sintatico() {

		
		t = lexico.getToken();
		programa();
		System.out.println(" Tabela de simbolos");
		System.out.println(tabSim);
	}
	
	public void mensagemErro(String msg) {
		System.err.println("Linha: " + t.getLinha() +
                ", Coluna: " + t.getColuna() + 
                msg);
	}
	
    public void programa() {
        if ((t.getClasse() == Classe.cPalRes) 
         && (t.getValor().getIdentificador().equals("program"))) {
            t = lexico.getToken();
            if (t.getClasse() == Classe.cId) {
                t = lexico.getToken();
                corpo();
                if (t.getClasse() == Classe.cPonto) {
                    t = lexico.getToken();
                } else {
                	mensagemErro(" O programa não foi encerrado, pois não foi encontrado o '.'");
                }
            } else {
            	mensagemErro(" Não foi encontrado o nome ");
            }
        } else {
        	mensagemErro(" Erro de inicialização no comando Begin ");
        }
    }
    
	public void corpo() {
		declara();
		if ((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("begin"))) {
			t = lexico.getToken();
			sentencas();
			if ((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("end"))) {
				t = lexico.getToken();
			}else {
				mensagemErro(" Erro de finalização End ");
			}
		}else {
			mensagemErro(" Erro de inicialização Begin");
		}
	}
	
	public void declara() {
		if ((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("var"))) {
			t = lexico.getToken();
			dvar();
			mais_dc();
		}
	}
	
	public void mais_dc() {
		if (t.getClasse() == Classe.cPtVirgula) {
			t = lexico.getToken();
			cont_dc();			
		}		else {
			mensagemErro(" Não encontrado ';' ");
		}
	}
	
	public void cont_dc() {
		if (t.getClasse() == Classe.cId) {
			dvar();
			mais_dc();
		}
	}	
	
	public void dvar() {
		variaveis();
		if (t.getClasse() == Classe.cDoisPontos) {
			t = lexico.getToken();
			tipo_var();			
		}else {
			mensagemErro("Não foi encontrado o ':' ");
		}
	}
	
	public void tipo_var() {
		if ((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("integer"))) {
			t = lexico.getToken();
		}else if ((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("real"))) {
			t = lexico.getToken();
		}else {
			mensagemErro(" Não encontrada declaração do tipo Int ");
		}
	}
	
	public void variaveis() {
		if (t.getClasse() == Classe.cId) {
			t = lexico.getToken();
			mais_var();
		}else {
			mensagemErro("Identificador não encontrado");
		}
	}
	
	public void mais_var() {
		if (t.getClasse() == Classe.cVirgula) {
			t = lexico.getToken();
			variaveis();
		}
	}
	
	public void sentencas() {
		comando();
		mais_sentencas();
	}
	
	public void mais_sentencas() {
		if (t.getClasse() == Classe.cPtVirgula) {
			t = lexico.getToken();
			cont_sentencas();
		}else {
			mensagemErro("; não encontrado");
		}
	}
	
	public void cont_sentencas() {
		if (((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("read"))) ||
				((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("write"))) ||
				((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("for"))) ||
				((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("repeat"))) ||
				((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("while"))) ||
				((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("if"))) ||
				((t.getClasse() == Classe.cId))
				) {
			sentencas();
		}
	}
	
	public void var_read() {
		if (t.getClasse() == Classe.cId) {
			t = lexico.getToken();
			//{A5}
			mais_var_read();
		}else {
			mensagemErro(" ID não encontrado ");
		}
	}
	
	public void mais_var_read() {
		if (t.getClasse() == Classe.cVirgula) {
			t = lexico.getToken();
			var_read();
		}
	}

	public void var_write() {
		if (t.getClasse() == Classe.cId) {
			t = lexico.getToken();
			mais_var_write();
		}else {
			mensagemErro(" ID não encontrado ");
		}
	}
	
	public void mais_var_write() {
		if (t.getClasse() == Classe.cVirgula) {
			t = lexico.getToken();
			var_write();
		}
	}

	public void comando() {
		if ((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("read"))){
			t = lexico.getToken();
			if (t.getClasse() == Classe.cParenteseEsq) {
				t = lexico.getToken();
				var_read();
				if (t.getClasse() == Classe.cParenteseDir) {
					t = lexico.getToken();
				}else {
					mensagemErro("')' Não foi encontrado");
				}
			}else {
				mensagemErro(" '(' Não foi encontrado");
			}
		}else
		if ((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("write"))){
			t = lexico.getToken();
			if (t.getClasse() == Classe.cParenteseEsq) {
				t = lexico.getToken();
				var_write();
				if (t.getClasse() == Classe.cParenteseDir) {
					t = lexico.getToken();
				}else {
					mensagemErro("')' Não foi encontrado\"");
				}
			}else {
				mensagemErro("'(' Não foi encontrado");
			}
		}else
		if ((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("for"))){
			t = lexico.getToken();
			if (t.getClasse() == Classe.cId) {
				t = lexico.getToken();
				if (t.getClasse() == Classe.cAtribuicao){
					t = lexico.getToken();
					expressao();
					if ((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("to"))){
						t = lexico.getToken();
						expressao();
						if ((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("do"))){
							t = lexico.getToken();
							if ((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("begin"))){
								t = lexico.getToken();
								sentencas();
								if ((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("end"))){
									t = lexico.getToken();
								}else {
									mensagemErro(" Erro na finalização do laço ");
								}									
							}else {
								mensagemErro(" Erro na inicialização do Laço");
							}							
						}else {
							mensagemErro("Não foi encontrado a função Do no Laço");
						}					
					}else {
						mensagemErro(" Não foi encontrado a função To no Laço");
					}					
				}else {
					mensagemErro(" Não foi encontrado a função := no Laço");
				}
			}else {
				mensagemErro(" Não foi encontrado a ID no Laço");
			}
		}else
		if ((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("repeat"))){
			t = lexico.getToken();
			sentencas();
			if ((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("until"))){
				t = lexico.getToken();
				if (t.getClasse() == Classe.cParenteseEsq){
					t = lexico.getToken();	
					condicao();
					if (t.getClasse() == Classe.cParenteseDir){
						t = lexico.getToken();
					}else {
						mensagemErro(" Não encontrado ) no repeat");
					}
				}else {
					mensagemErro(" Não encontrado ( no repeat");
				}
			}else {
				mensagemErro("Não encontrado Util no repeat");
			}				
		}
		else if ((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("while"))){
			t = lexico.getToken();	
			if (t.getClasse() == Classe.cParenteseEsq){
				t = lexico.getToken();	
				condicao();
				if (t.getClasse() == Classe.cParenteseDir){
					t = lexico.getToken();
					if ((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("do"))){
						t = lexico.getToken();
						if ((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("begin"))){
							t = lexico.getToken();
							sentencas();
							if ((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("end"))){
								t = lexico.getToken();
							}else {
								mensagemErro(" Não encontrado END no While");
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
		else if ((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("if"))){
			t = lexico.getToken();
			if (t.getClasse() == Classe.cParenteseEsq){
				t = lexico.getToken();	
				condicao();
				if (t.getClasse() == Classe.cParenteseDir){
					t = lexico.getToken();
					//{A17}
					if ((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("then"))){
						t = lexico.getToken();
						if ((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("begin"))){
							t = lexico.getToken();
							sentencas();
							if ((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("end"))){
								t = lexico.getToken();
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
		else if (t.getClasse() == Classe.cId){
			t = lexico.getToken();
			if (t.getClasse() == Classe.cAtribuicao){
				t = lexico.getToken();
				expressao();
			}
		}
	}

	public void condicao() {
		expressao();
		relacao();
		expressao();
	}
	
	public void pfalsa() {
		if ((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("else"))){
			t = lexico.getToken();
			if ((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("begin"))){
				t = lexico.getToken();
				sentencas();
				if ((t.getClasse() == Classe.cPalRes) && (t.getValor().getIdentificador().equals("end"))){
					t = lexico.getToken();
				}else {
					mensagemErro("Comando End não encontrado");
				}
			}else {
				mensagemErro("Comando Begin não encontrado");
			}
		}
	}
	
	public void relacao() {
		if (t.getClasse() == Classe.cIgual) {
			t = lexico.getToken();
		}else if (t.getClasse() == Classe.cMaior) {
			t = lexico.getToken();
		}else if (t.getClasse() == Classe.cMenor) {
			t = lexico.getToken();
		}else if (t.getClasse() == Classe.cMaiorIgual) {
			t = lexico.getToken();
		}else if (t.getClasse() == Classe.cMenorIgual) {
			t = lexico.getToken();
		}else if (t.getClasse() == Classe.cDiferente) {
			t = lexico.getToken();
		}else {
			mensagemErro("Operador matemático não encontrado");
		}
	}

	public void expressao() {
		termo();
		outros_termos();
	}

	public void outros_termos() {
		if (t.getClasse() == Classe.cSoma || t.getClasse() == Classe.cSub) {
			op_ad();
			termo();
			outros_termos();
		}
	}

	public void op_ad() {
		if (t.getClasse() == Classe.cSoma || t.getClasse() == Classe.cSub) {
			t = lexico.getToken();
		}else {
			mensagemErro(" + e/ou - Não encontrados");
		}
	}

	public void termo() {
		fator();
		mais_fatores();
	}

	public void mais_fatores() {
		if (t.getClasse() == Classe.cMult || t.getClasse() == Classe.cDiv) {
		op_mul();
		fator();
		mais_fatores();
		}
	}
	
	public void op_mul() {
		if (t.getClasse() == Classe.cMult || t.getClasse() == Classe.cDiv) {
			t = lexico.getToken();
		}else {
			mensagemErro(" * e/ou / não foram encontrados");
		}
	}


	public void fator() {
		if (t.getClasse() == Classe.cId) {
			t = lexico.getToken();
		}else if (t.getClasse() == Classe.cInt || t.getClasse() == Classe.cReal) {
			t = lexico.getToken();
		}else if (t.getClasse() == Classe.cParenteseEsq){
			t = lexico.getToken();
			expressao();
			if (t.getClasse() == Classe.cParenteseDir){
				t = lexico.getToken();
			}else {
				mensagemErro(") não encontrado");
			}
		}else {
			mensagemErro(" (ID, Num, Exp) não encontrados");
		}
	}
	
	private void criarTabelaPalavrasReservadas() {
        tabPalRes = new HashMap<String, String>();
        tabPalRes.put("read", "read");
		tabPalRes.put("write", "write");
		tabPalRes.put("and", "and");
		tabPalRes.put("array", "array");
		tabPalRes.put("begin", "begin");
		tabPalRes.put("case", "case");
		tabPalRes.put("const", "const");
		tabPalRes.put("div", "div");
		tabPalRes.put("do", "do");
		tabPalRes.put("downto", "downto");
		tabPalRes.put("else", "else");
		tabPalRes.put("end", "end");
		tabPalRes.put("file", "file");
		tabPalRes.put("for", "for");
		tabPalRes.put("function", "function");
		tabPalRes.put("goto", "goto");
		tabPalRes.put("if", "if");
		tabPalRes.put("in", "in");
		tabPalRes.put("label", "label");
		tabPalRes.put("mod", "mod");
		tabPalRes.put("nil", "nil");
		tabPalRes.put("not", "not");
		tabPalRes.put("of", "of");
		tabPalRes.put("or", "or");
		tabPalRes.put("packed", "packed");
		tabPalRes.put("procedure", "procedure");
		tabPalRes.put("program", "program");
		tabPalRes.put("record", "record");
		tabPalRes.put("repeat", "repeat");
		tabPalRes.put("set", "set");
		tabPalRes.put("then", "then");
		tabPalRes.put("to", "to");
		tabPalRes.put("type", "type");
		tabPalRes.put("until", "until");
		tabPalRes.put("var", "var");
		tabPalRes.put("while", "while");
		tabPalRes.put("with", "with");
		tabPalRes.put("real", "real");
		tabPalRes.put("integer", "integer");
    }

}
