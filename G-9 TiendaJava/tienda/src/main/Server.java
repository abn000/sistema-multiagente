/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import paquete1.Producto;
import paquete1.Tienda;
import paquete1.Cliente;

/**
 *
 * @author mg_4_
 */
public class Server {

    public ArrayList<Tienda> listaTiendas;
    public String ipLocal;
    public int puertoLocal;
    public int puertoMonitor;
    public String ipMonitor;

    public Server(String ipLocal, int puertoLocal, String ipMonitor, int puertoMonitor) {
        this.listaTiendas = new ArrayList<Tienda>();
        this.ipLocal = ipLocal;
        this.puertoLocal = puertoLocal;
        this.ipMonitor = ipMonitor;
        this.puertoMonitor = puertoMonitor;
    }

    private Tienda encuentraTienda(int id) {
        for (Tienda t : this.listaTiendas) {
            if (t.id == id) {
                return t;
            }
        }
        return null;
    }

    public void recibeInicializacion(int idTienda, ArrayList<Producto> listaProductos) {
        System.out.println("Se crea la tienda " + idTienda + " con los productos " + listaProductos);
        this.listaTiendas.add(new Tienda(listaProductos, idTienda, this.ipLocal, this.puertoLocal));
    }

    public void entraCliente(int idCliente, String ipCliente, int puertoCliente, ArrayList<Producto> listaCompra, int idTienda) {
        Tienda t = this.encuentraTienda(idTienda);
        if (t != null) {
            System.out.println("Cliente " + idCliente + " ha entrado a la tienda " + idTienda + " con la lista de la compra " + listaCompra);
            t.entraUnNuevoCliente(new Cliente(ipCliente, puertoCliente, idCliente));
            String mensaje = t.Comprar(listaCompra);
            SendPost.enviarMensaje(new Agente(ipCliente, puertoCliente, "cliente", idCliente), new Agente(t.ip, t.puerto, "tienda", t.id), new Agente(ipMonitor, puertoMonitor, "monitor", 0), "productosDisponibles", mensaje);
            if (t.compruebaStock()) {
                SendPost.enviarMensajeMonitor(new Agente(ipLocal, puertoLocal, "tienda", t.id), new Agente(ipMonitor, puertoMonitor, "monitor", 0), "finalizacion_tienda", "");
                //listaTiendas.remove(t);
            }
        } else {
            System.out.println("El cliente " + idCliente + " ha intentado entrar a la tienda " + idTienda + " pero está cerrada");
            SendPost.enviarMensaje(new Agente(ipCliente, puertoCliente, "cliente", idCliente), new Agente(t.ip, t.puerto, "tienda", t.id), new Agente(ipMonitor, puertoMonitor, "monitor", 0), "productosDisponibles", "<lista_productos></lista_productos>");
        }

    }

    public void clientePideTiendas(int idCliente, String ipCliente, int puertoCliente, ArrayList<Tienda> tiendasConocidas, int idTienda) {
        Tienda t = this.encuentraTienda(idTienda);
        if (t != null) {
            System.out.println("El cliente " + idCliente + " pregunta a la tienda " + t.id + " las tiendas conocidad y el cliente conoce" + tiendasConocidas);            
            for (int i=0;i<t.nClientes;i++) {
                if (t.listaClientes.get(i).id == idCliente) {
                    t.listaClientes.get(i).AddTiendasConocidas(tiendasConocidas);
                    t.saleUnCliente(t.listaClientes.get(i));
                }
            }
            String mensaje = t.DevolverListaTiendas();
            SendPost.enviarMensaje(new Agente(ipCliente, puertoCliente, "cliente", idCliente), new Agente(t.ip, t.puerto, "tienda", t.id), new Agente(ipMonitor, puertoMonitor, "monitor", 0), "tiendasConocidas", mensaje);
        }else{
            System.out.println("El cliente " + idCliente + " ha intentado pedir tiendas conocidas a la tienda " + idTienda + " pero está cerrada");
            SendPost.enviarMensaje(new Agente(ipCliente, puertoCliente, "cliente", idCliente), new Agente(t.ip, t.puerto, "tienda", t.id), new Agente(ipMonitor, puertoMonitor, "monitor", 0), "productosDisponibles", "<lista_tiendas></lista_tiendas>");
        }

    }

}
