package co.edu.unbosque.biohub.taxonomia.ficha;

import co.edu.unbosque.biohub.taxonomia.atributo.AtributoTaxonomico;

/**
 * Un atributo dentro de una ficha con su valor (el valor puede ser null).
 */
public class ValorAtributo {

    private AtributoTaxonomico atributo;
    private String valor;

    protected ValorAtributo() { }

    ValorAtributo(AtributoTaxonomico atributo, String valor) {
        this.atributo = atributo;
        this.valor = valor;
    }

    void cambiarValor(String valor) {
        this.valor = valor;
    }

    public AtributoTaxonomico getAtributo() { return atributo; }
    public String getValor() { return valor; }
}
