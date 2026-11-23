package co.edu.unbosque.biohub.historial.cambio;

public enum TipoCambio {
    CREACION,         // primera versión del registro
    MODIFICACION,     // cambia el valor de un campo
    RECLASIFICACION,  // cambia la especie de un hallazgo: exige autorizador y motivo
    DESACTIVACION     // la ficha deja de estar activa (se conserva su historial)
}
