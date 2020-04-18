/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataaccess;

import dataaccess.exceptions.IllegalOrphanException;
import dataaccess.exceptions.NonexistentEntityException;
import dataaccess.exceptions.RollbackFailureException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import entidad.Categoria;
import entidad.Ganancia;
import entidad.Compradetalle;
import entidad.Producto;
import java.util.ArrayList;
import java.util.Collection;
import entidad.Ventadetalle;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author Saul
 */
public class ProductoJpaController implements Serializable {

    public ProductoJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Producto producto) throws RollbackFailureException, Exception {
        if (producto.getCompradetalleCollection() == null) {
            producto.setCompradetalleCollection(new ArrayList<Compradetalle>());
        }
        if (producto.getVentadetalleCollection() == null) {
            producto.setVentadetalleCollection(new ArrayList<Ventadetalle>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Categoria categoriaid = producto.getCategoriaid();
            if (categoriaid != null) {
                categoriaid = em.getReference(categoriaid.getClass(), categoriaid.getCategoriaid());
                producto.setCategoriaid(categoriaid);
            }
            Ganancia ganancia = producto.getGanancia();
            if (ganancia != null) {
                ganancia = em.getReference(ganancia.getClass(), ganancia.getGananciaid());
                producto.setGanancia(ganancia);
            }
            Collection<Compradetalle> attachedCompradetalleCollection = new ArrayList<Compradetalle>();
            for (Compradetalle compradetalleCollectionCompradetalleToAttach : producto.getCompradetalleCollection()) {
                compradetalleCollectionCompradetalleToAttach = em.getReference(compradetalleCollectionCompradetalleToAttach.getClass(), compradetalleCollectionCompradetalleToAttach.getCompradetallePK());
                attachedCompradetalleCollection.add(compradetalleCollectionCompradetalleToAttach);
            }
            producto.setCompradetalleCollection(attachedCompradetalleCollection);
            Collection<Ventadetalle> attachedVentadetalleCollection = new ArrayList<Ventadetalle>();
            for (Ventadetalle ventadetalleCollectionVentadetalleToAttach : producto.getVentadetalleCollection()) {
                ventadetalleCollectionVentadetalleToAttach = em.getReference(ventadetalleCollectionVentadetalleToAttach.getClass(), ventadetalleCollectionVentadetalleToAttach.getVentadetallePK());
                attachedVentadetalleCollection.add(ventadetalleCollectionVentadetalleToAttach);
            }
            producto.setVentadetalleCollection(attachedVentadetalleCollection);
            em.persist(producto);
            if (categoriaid != null) {
                categoriaid.getProductoCollection().add(producto);
                categoriaid = em.merge(categoriaid);
            }
            if (ganancia != null) {
                Producto oldProductoidOfGanancia = ganancia.getProductoid();
                if (oldProductoidOfGanancia != null) {
                    oldProductoidOfGanancia.setGanancia(null);
                    oldProductoidOfGanancia = em.merge(oldProductoidOfGanancia);
                }
                ganancia.setProductoid(producto);
                ganancia = em.merge(ganancia);
            }
            for (Compradetalle compradetalleCollectionCompradetalle : producto.getCompradetalleCollection()) {
                Producto oldProductoOfCompradetalleCollectionCompradetalle = compradetalleCollectionCompradetalle.getProducto();
                compradetalleCollectionCompradetalle.setProducto(producto);
                compradetalleCollectionCompradetalle = em.merge(compradetalleCollectionCompradetalle);
                if (oldProductoOfCompradetalleCollectionCompradetalle != null) {
                    oldProductoOfCompradetalleCollectionCompradetalle.getCompradetalleCollection().remove(compradetalleCollectionCompradetalle);
                    oldProductoOfCompradetalleCollectionCompradetalle = em.merge(oldProductoOfCompradetalleCollectionCompradetalle);
                }
            }
            for (Ventadetalle ventadetalleCollectionVentadetalle : producto.getVentadetalleCollection()) {
                Producto oldProductoOfVentadetalleCollectionVentadetalle = ventadetalleCollectionVentadetalle.getProducto();
                ventadetalleCollectionVentadetalle.setProducto(producto);
                ventadetalleCollectionVentadetalle = em.merge(ventadetalleCollectionVentadetalle);
                if (oldProductoOfVentadetalleCollectionVentadetalle != null) {
                    oldProductoOfVentadetalleCollectionVentadetalle.getVentadetalleCollection().remove(ventadetalleCollectionVentadetalle);
                    oldProductoOfVentadetalleCollectionVentadetalle = em.merge(oldProductoOfVentadetalleCollectionVentadetalle);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Producto producto) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Producto persistentProducto = em.find(Producto.class, producto.getProductoid());
            Categoria categoriaidOld = persistentProducto.getCategoriaid();
            Categoria categoriaidNew = producto.getCategoriaid();
            Ganancia gananciaOld = persistentProducto.getGanancia();
            Ganancia gananciaNew = producto.getGanancia();
            Collection<Compradetalle> compradetalleCollectionOld = persistentProducto.getCompradetalleCollection();
            Collection<Compradetalle> compradetalleCollectionNew = producto.getCompradetalleCollection();
            Collection<Ventadetalle> ventadetalleCollectionOld = persistentProducto.getVentadetalleCollection();
            Collection<Ventadetalle> ventadetalleCollectionNew = producto.getVentadetalleCollection();
            List<String> illegalOrphanMessages = null;
            if (gananciaOld != null && !gananciaOld.equals(gananciaNew)) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("You must retain Ganancia " + gananciaOld + " since its productoid field is not nullable.");
            }
            for (Compradetalle compradetalleCollectionOldCompradetalle : compradetalleCollectionOld) {
                if (!compradetalleCollectionNew.contains(compradetalleCollectionOldCompradetalle)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Compradetalle " + compradetalleCollectionOldCompradetalle + " since its producto field is not nullable.");
                }
            }
            for (Ventadetalle ventadetalleCollectionOldVentadetalle : ventadetalleCollectionOld) {
                if (!ventadetalleCollectionNew.contains(ventadetalleCollectionOldVentadetalle)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Ventadetalle " + ventadetalleCollectionOldVentadetalle + " since its producto field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (categoriaidNew != null) {
                categoriaidNew = em.getReference(categoriaidNew.getClass(), categoriaidNew.getCategoriaid());
                producto.setCategoriaid(categoriaidNew);
            }
            if (gananciaNew != null) {
                gananciaNew = em.getReference(gananciaNew.getClass(), gananciaNew.getGananciaid());
                producto.setGanancia(gananciaNew);
            }
            Collection<Compradetalle> attachedCompradetalleCollectionNew = new ArrayList<Compradetalle>();
            for (Compradetalle compradetalleCollectionNewCompradetalleToAttach : compradetalleCollectionNew) {
                compradetalleCollectionNewCompradetalleToAttach = em.getReference(compradetalleCollectionNewCompradetalleToAttach.getClass(), compradetalleCollectionNewCompradetalleToAttach.getCompradetallePK());
                attachedCompradetalleCollectionNew.add(compradetalleCollectionNewCompradetalleToAttach);
            }
            compradetalleCollectionNew = attachedCompradetalleCollectionNew;
            producto.setCompradetalleCollection(compradetalleCollectionNew);
            Collection<Ventadetalle> attachedVentadetalleCollectionNew = new ArrayList<Ventadetalle>();
            for (Ventadetalle ventadetalleCollectionNewVentadetalleToAttach : ventadetalleCollectionNew) {
                ventadetalleCollectionNewVentadetalleToAttach = em.getReference(ventadetalleCollectionNewVentadetalleToAttach.getClass(), ventadetalleCollectionNewVentadetalleToAttach.getVentadetallePK());
                attachedVentadetalleCollectionNew.add(ventadetalleCollectionNewVentadetalleToAttach);
            }
            ventadetalleCollectionNew = attachedVentadetalleCollectionNew;
            producto.setVentadetalleCollection(ventadetalleCollectionNew);
            producto = em.merge(producto);
            if (categoriaidOld != null && !categoriaidOld.equals(categoriaidNew)) {
                categoriaidOld.getProductoCollection().remove(producto);
                categoriaidOld = em.merge(categoriaidOld);
            }
            if (categoriaidNew != null && !categoriaidNew.equals(categoriaidOld)) {
                categoriaidNew.getProductoCollection().add(producto);
                categoriaidNew = em.merge(categoriaidNew);
            }
            if (gananciaNew != null && !gananciaNew.equals(gananciaOld)) {
                Producto oldProductoidOfGanancia = gananciaNew.getProductoid();
                if (oldProductoidOfGanancia != null) {
                    oldProductoidOfGanancia.setGanancia(null);
                    oldProductoidOfGanancia = em.merge(oldProductoidOfGanancia);
                }
                gananciaNew.setProductoid(producto);
                gananciaNew = em.merge(gananciaNew);
            }
            for (Compradetalle compradetalleCollectionNewCompradetalle : compradetalleCollectionNew) {
                if (!compradetalleCollectionOld.contains(compradetalleCollectionNewCompradetalle)) {
                    Producto oldProductoOfCompradetalleCollectionNewCompradetalle = compradetalleCollectionNewCompradetalle.getProducto();
                    compradetalleCollectionNewCompradetalle.setProducto(producto);
                    compradetalleCollectionNewCompradetalle = em.merge(compradetalleCollectionNewCompradetalle);
                    if (oldProductoOfCompradetalleCollectionNewCompradetalle != null && !oldProductoOfCompradetalleCollectionNewCompradetalle.equals(producto)) {
                        oldProductoOfCompradetalleCollectionNewCompradetalle.getCompradetalleCollection().remove(compradetalleCollectionNewCompradetalle);
                        oldProductoOfCompradetalleCollectionNewCompradetalle = em.merge(oldProductoOfCompradetalleCollectionNewCompradetalle);
                    }
                }
            }
            for (Ventadetalle ventadetalleCollectionNewVentadetalle : ventadetalleCollectionNew) {
                if (!ventadetalleCollectionOld.contains(ventadetalleCollectionNewVentadetalle)) {
                    Producto oldProductoOfVentadetalleCollectionNewVentadetalle = ventadetalleCollectionNewVentadetalle.getProducto();
                    ventadetalleCollectionNewVentadetalle.setProducto(producto);
                    ventadetalleCollectionNewVentadetalle = em.merge(ventadetalleCollectionNewVentadetalle);
                    if (oldProductoOfVentadetalleCollectionNewVentadetalle != null && !oldProductoOfVentadetalleCollectionNewVentadetalle.equals(producto)) {
                        oldProductoOfVentadetalleCollectionNewVentadetalle.getVentadetalleCollection().remove(ventadetalleCollectionNewVentadetalle);
                        oldProductoOfVentadetalleCollectionNewVentadetalle = em.merge(oldProductoOfVentadetalleCollectionNewVentadetalle);
                    }
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = producto.getProductoid();
                if (findProducto(id) == null) {
                    throw new NonexistentEntityException("The producto with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Long id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Producto producto;
            try {
                producto = em.getReference(Producto.class, id);
                producto.getProductoid();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The producto with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Ganancia gananciaOrphanCheck = producto.getGanancia();
            if (gananciaOrphanCheck != null) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Producto (" + producto + ") cannot be destroyed since the Ganancia " + gananciaOrphanCheck + " in its ganancia field has a non-nullable productoid field.");
            }
            Collection<Compradetalle> compradetalleCollectionOrphanCheck = producto.getCompradetalleCollection();
            for (Compradetalle compradetalleCollectionOrphanCheckCompradetalle : compradetalleCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Producto (" + producto + ") cannot be destroyed since the Compradetalle " + compradetalleCollectionOrphanCheckCompradetalle + " in its compradetalleCollection field has a non-nullable producto field.");
            }
            Collection<Ventadetalle> ventadetalleCollectionOrphanCheck = producto.getVentadetalleCollection();
            for (Ventadetalle ventadetalleCollectionOrphanCheckVentadetalle : ventadetalleCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Producto (" + producto + ") cannot be destroyed since the Ventadetalle " + ventadetalleCollectionOrphanCheckVentadetalle + " in its ventadetalleCollection field has a non-nullable producto field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Categoria categoriaid = producto.getCategoriaid();
            if (categoriaid != null) {
                categoriaid.getProductoCollection().remove(producto);
                categoriaid = em.merge(categoriaid);
            }
            em.remove(producto);
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Producto> findProductoEntities() {
        return findProductoEntities(true, -1, -1);
    }

    public List<Producto> findProductoEntities(int maxResults, int firstResult) {
        return findProductoEntities(false, maxResults, firstResult);
    }

    private List<Producto> findProductoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Producto.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Producto findProducto(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Producto.class, id);
        } finally {
            em.close();
        }
    }

    public int getProductoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Producto> rt = cq.from(Producto.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
