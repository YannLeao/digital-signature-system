package com.example.backend.service.document;

import com.example.backend.exception.PdfValidationException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDDestinationOrAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript;
import org.apache.pdfbox.pdmodel.interactive.action.PDDocumentCatalogAdditionalActions;

final class PdfValidationCore {

    private static final String ERROR_CODE = "DOC_001";

    private PdfValidationCore() {
    }

    static void validateStructure(byte[] bytes) {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            checkAction(document.getDocumentCatalog().getOpenAction());

            PDDocumentCatalogAdditionalActions catalogActions = document.getDocumentCatalog().getActions();
            if (catalogActions != null) {
                checkAction(catalogActions.getDP());
                checkAction(catalogActions.getDS());
                checkAction(catalogActions.getWC());
                checkAction(catalogActions.getWP());
                checkAction(catalogActions.getWS());
            }

            for (PDPage page : document.getPages()) {
                if (page.getActions() != null) {
                    checkAction(page.getActions().getO());
                    checkAction(page.getActions().getC());
                }
            }
        } catch (PdfValidationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw invalid("Nao foi possivel processar o PDF para validacao.");
        }
    }

    private static void checkAction(PDDestinationOrAction action) {
        if (action instanceof PDActionJavaScript) {
            throw invalid("PDF rejeitado: contem acoes embutidas suspeitas.");
        }
    }

    private static void checkAction(PDAction action) {
        if (action instanceof PDActionJavaScript) {
            throw invalid("PDF rejeitado: contem acoes embutidas suspeitas.");
        }
    }

    private static PdfValidationException invalid(String message) {
        return new PdfValidationException(ERROR_CODE, message);
    }
}
