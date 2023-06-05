package compiladores.reconhecedor;

public class TabSimRec {

	private String lexema;
	private Categoria categoria;

	public String getLexema() {
		return lexema;
	}

	public void setLexema(String lexema) {
		this.lexema = lexema;
	}

	public Categoria getCategoria() {
		return categoria;
	}

	public void setCategoria(Categoria categoria) {
		this.categoria = categoria;
	}

	@Override
	public String toString() {
		return "lexema: " + lexema + "\ncategoria: " + categoria;
	}

}
