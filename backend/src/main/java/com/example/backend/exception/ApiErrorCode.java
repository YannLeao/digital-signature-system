package com.example.backend.exception;

public enum ApiErrorCode {
	VAL_001("Dados invalidos."),
	VAL_002("Parametro invalido."),
	VAL_003("Regra de negocio violada."),
	AUTH_001("Credenciais invalidas."),
	AUTH_002("Acesso nao autorizado."),
	AUTH_003("Token invalido ou revogado."),
	AUTH_004("Codigo de verificacao invalido."),
	AUTH_005("Verificacao temporariamente bloqueada."),
	SEC_001("Requisicao bloqueada por politica de seguranca."),
	DOC_001("Documento invalido."),
	SYS_001("Erro interno inesperado."),
	SYS_002("Recurso nao encontrado.");

	private final String defaultMessage;

	ApiErrorCode(String defaultMessage) {
		this.defaultMessage = defaultMessage;
	}

	public String defaultMessage() {
		return defaultMessage;
	}
}
