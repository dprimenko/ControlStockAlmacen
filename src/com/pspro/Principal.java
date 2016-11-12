package com.pspro;

import java.util.Random;

class Almacen {	
	
	// Constantes
	public static final int MAX_STOCK = 20000;
	public static final int ENVIOS_MAX_DIA = 3;
	
	// Campos
	private int stockActual;
	private int enviosRealizados;
	private boolean pedidoRealizado;
	private volatile boolean almacenDisponible;
	
	public boolean getAlmacenDisponible() {
		return this.almacenDisponible;
	}
	
	public int getEnviosRealizados() {
		return this.enviosRealizados;
	}
	
	public boolean getPedidoRealizado() {
		return this.pedidoRealizado;
	}
	
	/**
	 * Asigna el valor a la variable pedidoRealizado y notifica
	 * al hilo Envio de que esta cambió
	 * @param valor
	 */
	
	public synchronized void setPedidoRealizado(boolean valor) {
		pedidoRealizado = valor;
		notify();
	
	}
	public Almacen() {
		this.stockActual = 8000;
		this.enviosRealizados = 0;
		this.almacenDisponible = true;
		this.pedidoRealizado = false;
	}
	
	/**
	 * Método que incrementa los enviosRealizados cada dia y avisa,
	 * si llega a 3, al hilo Retirada que vuelva a comprobar la variable
	 * enviosRealizados
	 */
	public synchronized void envioRealizado() {
		
		enviosRealizados += 1;
		
		if (enviosRealizados == ENVIOS_MAX_DIA) {
			notify();
		}	
	}	
	
	public void retiradaStock(int valor) {
		stockActual -= valor;
	}
	
	public void envioStock(int valor) {
		stockActual += valor;
	}	
	
	
	/**
	 * Bloquea el hilo Envio hasta que el pedido correspondiente se realice
	 */
	public void envioDebeEsperar() {
		if (!(this.pedidoRealizado)) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Bloquea el hilo Retirada hasta que los 3 envios diarios se realicen
	 */
	public void retiradaDebeEsperar() {
		if (this.enviosRealizados < ENVIOS_MAX_DIA) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Método que verifica que el stock no ha sobrepasado sus limites
	 * y lo escribe por pantalla
	 */
	public synchronized void comprobarStockActual() {
		
		if (this.stockActual < 0) {
			this.almacenDisponible = false;
			System.err.println("No hay suficientes piezas en el almacén!");
		}
		
		else if (this.stockActual > MAX_STOCK) {
			this.almacenDisponible = false;
			System.err.println("Ya no cabe nada en el almacén!");
		}
		
		else {
			System.out.println("En el almacen hay " + this.stockActual + " piezas\n");
		}
		
	}
}

class Retirada extends Thread {
	
	private int diasTranscurridos;
	private Almacen almacen;
	private Random random;
	private int retiradaAlmacen;
	
	public Retirada(Almacen almacen) {
		diasTranscurridos = 0;
		this.almacen = almacen;
		retiradaAlmacen = 0;
		random = new Random();
	}
	
	@Override
	public void run() {				
		while(almacen.getAlmacenDisponible()) {		
			this.diasTranscurridos++;
			retiradaAlmacen = 2001 + random.nextInt(499);
			
			System.out.println("--------------------------------");
			System.out.println("\n[Dia " + diasTranscurridos + "]");
			
			almacen.retiradaStock(retiradaAlmacen);	
			
			System.out.println("Pedido de " + retiradaAlmacen + " piezas");		
			
			almacen.comprobarStockActual();
			almacen.setPedidoRealizado(true);
			
			try {
				sleep(2400);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}		
			
			almacen.retiradaDebeEsperar();
		}	
	}
	
}

class Envio extends Thread {
	
	private Almacen almacen;
	private int envioCantidad;
	private Random random;
	
	public Envio(Almacen almacen) {
		this.almacen = almacen;
		random = new Random();
	}
	
	@Override
	public void run() {
		
		while(almacen.getAlmacenDisponible()) {
			
			almacen.envioDebeEsperar();
			
			envioCantidad = (401 + random.nextInt(599));		
			almacen.envioStock(envioCantidad);				
			almacen.envioRealizado();
			System.out.println("Llegan " + envioCantidad + " piezas");	
			almacen.comprobarStockActual();
			try {
				sleep(800);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	}
}

public class Principal {
	
	public static void main(String[] args) throws InterruptedException {
		
		Almacen almacen = new Almacen();
		
		Retirada fabrica = new Retirada(almacen);
		Envio cargamento = new Envio(almacen);
		
		fabrica.start();
		cargamento.start();
		
		fabrica.join();
		cargamento.join();		
	}

}
