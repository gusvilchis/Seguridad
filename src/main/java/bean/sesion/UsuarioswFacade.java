/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean.sesion;

import entidad.Usuariosw;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Saul
 */
@Stateless
public class UsuarioswFacade extends AbstractFacade<Usuariosw> {

    @PersistenceContext(unitName = "com.mycompany_Proveedoressw_war_1.0-SNAPSHOTPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public UsuarioswFacade() {
        super(Usuariosw.class);
    }
    
}
