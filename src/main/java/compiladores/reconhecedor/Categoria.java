package compiladores.reconhecedor;

public enum Categoria {
	FUNCAO("Função"),
	VARIAVEL("Variável"),
	PARAMETRO("Parâmetro"),
	PROCEDIMENTO("Procedimento"),
	TIPO("Tipo");
	
	private String descricao;
	
	private Categoria(String descricao) {
		this.descricao = descricao;
	}
	
	public String getDescricao() {
		return descricao;
	}
	
}