package compiladores.reconhecedor;

public class Token {
	private Classe classe;
	private Valor valor;
	private int linha;
	private int coluna;

	private int tamanhoToken;

	public Valor getValor() {
		return valor;
	}

	public int getLinha() {
		return linha;
	}

	public int getColuna() {
		return coluna;
	}

	public int getTamanhoToken() {
		return tamanhoToken;
	}

	public void setTamanhoToken(int tamanhoToken) {
		this.tamanhoToken = tamanhoToken;
	}

	public Classe getClasse() {
		return classe;
	}

	public void setClasse(Classe classe) {
		this.classe = classe;
	}

	public void setValor(Valor valor) {
		this.valor = valor;
	}

	public void setLinha(int linha) {
		this.linha = linha;
	}

	public void setColuna(int coluna) {
		this.coluna = coluna;
	}

	@Override
	public String toString() {
		return "Token{" +
				"classe=" + classe +
				", valor=" + valor +
				", linha=" + linha +
				", coluna=" + coluna +
				'}';
	}

}
