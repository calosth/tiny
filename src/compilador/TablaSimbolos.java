package compilador;

import java.util.*;


import ast.*;

public class TablaSimbolos {
	private HashMap<String, HashMap<String, RegistroSimbolo>> tabla;
	private HashMap<String, RegistroSimbolo> tablaAmbito;
	private int direccion;  //Contador de las localidades de memoria asignadas a la tabla
	private String ultimoTipo;
	private String ultimoAmbito;
	public TablaSimbolos() {
		super();
		tabla = new HashMap<String, HashMap<String, RegistroSimbolo>>();
		direccion=0;
	}

	public void cargarTabla(NodoBase raiz){
		while (raiz != null) {
	    /* Hago el recorrido recursivo */
	    if (raiz instanceof  NodoIf){
	    	cargarTabla(((NodoIf)raiz).getPrueba());
	    	cargarTabla(((NodoIf)raiz).getParteThen());
	    	if(((NodoIf)raiz).getParteElse()!=null){
	    		cargarTabla(((NodoIf)raiz).getParteElse());
	    	}
	    }
	    else if (raiz instanceof  NodoRepeat){
	    	cargarTabla(((NodoRepeat)raiz).getCuerpo());
	    	cargarTabla(((NodoRepeat)raiz).getPrueba());
	    }
	    else if (raiz instanceof  NodoAsignacion){
	    	cargarTabla(((NodoAsignacion)raiz).getExpresion());
	    }
	    else if (raiz instanceof  NodoEscribir)
	    	cargarTabla(((NodoEscribir)raiz).getExpresion());
	    else if (raiz instanceof NodoOperacion){
	    	cargarTabla(((NodoOperacion)raiz).getOpIzquierdo());
	    	cargarTabla(((NodoOperacion)raiz).getOpDerecho());
	    }
	    else if (raiz instanceof NodoDeclaracion) {
	    	// Inserto el primer identificador y busco guardo el tipo de la declaracion que estoy recorriendo
	    	ultimoTipo = ((NodoDeclaracion)raiz).getTipo();  
	    	NodoBase nodo=  ((NodoDeclaracion) raiz).getVariable(); 	
	    	InsertarSimbolo(((NodoIdentificador)nodo).getNombre(),ultimoAmbito, ultimoTipo);
	    	cargarIdentificadores((NodoIdentificador)nodo);
	    	
	    } 	    
	    else if (raiz instanceof NodoFuncion) {
	    	ultimoAmbito = ((NodoFuncion)raiz).getNombre();	// Cambio el ambito cuando entro a una funcion    	
	    	cargarTabla(((NodoFuncion)raiz).getSent());
	    	cargarTabla(((NodoFuncion)raiz).getArgs());
	    } 
	    else if (raiz instanceof NodoProgram) {
	    	if(((NodoProgram)raiz).getFunctions()!=null){
	    		cargarTabla(((NodoProgram)raiz).getFunctions());
	    	}	
	    	ultimoAmbito = "main";
	    	cargarTabla(((NodoProgram)raiz).getMain());
	    } 	    	
	    raiz = raiz.getHermanoDerecha();
		
	  }
	}
	
	public void cargarIdentificadores(NodoIdentificador identificador){
    	InsertarSimbolo(identificador.getNombre(),ultimoAmbito, ultimoTipo); 
    	if(identificador.getSiguiente() != null) // Compruebo que el identificador tenga hermanos 
    		cargarIdentificadores((NodoIdentificador)identificador.getSiguiente());	  	
	}
	
	//true es nuevo no existe se insertara, false ya existe NO se vuelve a insertar 
	public boolean InsertarSimbolo(String identificador, String ambito, String tipo){
		RegistroSimbolo simbolo;
		

		// Si existe el ambito busco su tabla con los simbolos
		if(tabla.containsKey(ambito)){
			tablaAmbito = tabla.get(ambito);
			if(tablaAmbito.containsKey(identificador)){
				return false; // si existe no lo creo
			} else {
				simbolo= new RegistroSimbolo(identificador,-1, direccion++, tipo);
				tablaAmbito.put(identificador, simbolo);
				return true;
			}	
		} else {
			// Si el ambito no existe creo la nueva tabla para ambito
			tablaAmbito = new HashMap<String, RegistroSimbolo>();
			simbolo= new RegistroSimbolo(identificador,-1, direccion++, tipo);
			tablaAmbito.put(identificador, simbolo);			
			tabla.put(ambito, tablaAmbito);
			return true;
		}
	}
	
	public RegistroSimbolo BuscarSimbolo(String identificador, HashMap<String, RegistroSimbolo> tablaAmbito ){
		RegistroSimbolo simbolo=(RegistroSimbolo)tablaAmbito.get(identificador);
		return simbolo;
	}	
	
	public void ImprimirClaves(){
		System.out.println("*** Tabla de Simbolos ***");
		for( Iterator <String>it = tabla.keySet().iterator(); it.hasNext();) { 
            String ambito = (String)it.next();
            System.out.println("Ambito: " + ambito);            
    		this.tablaAmbito = tabla.get(ambito);
    		for( Iterator <String>iteradorInterno = tablaAmbito.keySet().iterator(); iteradorInterno.hasNext();) {
    			String identificador = (String)iteradorInterno.next();
    			System.out.println("\tConsegui Key: "+ BuscarSimbolo(identificador, tablaAmbito).getIdentificador() +" con direccion: " + BuscarSimbolo(identificador, tablaAmbito).getDireccionMemoria() +" tipo: "+BuscarSimbolo(identificador, tablaAmbito).getTipo() + " numero de linea: "+BuscarSimbolo(identificador, tablaAmbito).getNumLinea());
    		}   
		}
	}

	public int getDireccion(String Clave){
//		return BuscarSimbolo(Clave).getDireccionMemoria();
		return 1;
	}
	
	public String getTipo(String ambito, String identificador){
		this.tablaAmbito = tabla.get(ambito);
		RegistroSimbolo simbolo=(RegistroSimbolo)tablaAmbito.get(identificador);
		return simbolo.getTipo();
	}
	
	public boolean buscarTabla(String ambito, String identificador){
		if(tabla.containsKey(ambito)){
			tablaAmbito = tabla.get(ambito);
			if(tablaAmbito.containsKey(identificador)){
				return true; // si existe no lo creo
			} else {
				return false;
			}	
		} else {
			return false;
		}		
	}
	/*
	 * TODO:
	 * 1. Crear lista con las lineas de codigo donde la variable es usada.
	 * */
}
