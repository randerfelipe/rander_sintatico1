package compiladores.reconhecedor;

public class Token {

	private Classe classe;
	private Valor valor;
	private int linha;
	private int coluna;
	private int tamanhoLexema;

	public Classe getClasse() {
		return classe;
	}

	public void setClasse(Classe classe) {
		this.classe = classe;
	}
	
	public void setTamanhoLexema(int tamanhoLexema) {
		this.tamanhoLexema = tamanhoLexema;
	}
	
	public int getTamanhoLexema() {
		return tamanhoLexema;
	}

	public Valor getValor() {
		return valor;
	}

	public void setValor(Valor valor) {
		this.valor = valor;
	}
	
	public int getColuna() {
		return coluna;
	}
	
	public int getLinha() {
		return linha;
	}
	
	public Token() {
		
	}
	
	public Token(int linha, int coluna) {
		this.linha = linha;
		this.coluna = coluna;		
	}
	
	public Token(int linha, int coluna, Classe classe) {
		this.linha = linha;
		this.coluna = coluna;
		this.classe = classe;		
	}
	
	

	@Override
	public String toString() {
		String retorno = "Classe: " + classe;
		if (classe.equals(Classe.cId)) {
			retorno += ", Valor: " + valor.getIdentificador();
		} else if (classe.equals(Classe.cInt)) {
			retorno += ", Valor: " + valor.getInteiro();
		} else if (classe.equals(Classe.cReal)) {
			retorno += ", Valor: " + valor.getDecimal();
		} else if (classe.equals(Classe.cPalRes)) {
			retorno += ", Valor: " + valor.getIdentificador();
		} 
		retorno += " , coluna: " + coluna + " , linha: " + linha +" , tamanho: " + tamanhoLexema;
		return retorno;
	}

}

