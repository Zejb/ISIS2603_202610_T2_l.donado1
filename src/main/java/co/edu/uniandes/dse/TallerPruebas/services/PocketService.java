package co.edu.uniandes.dse.TallerPruebas.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniandes.dse.TallerPruebas.entities.AccountEntity;
import co.edu.uniandes.dse.TallerPruebas.entities.PocketEntity;
import co.edu.uniandes.dse.TallerPruebas.exceptions.BusinessLogicException;
import co.edu.uniandes.dse.TallerPruebas.exceptions.EntityNotFoundException;
import co.edu.uniandes.dse.TallerPruebas.repositories.AccountRepository;
import co.edu.uniandes.dse.TallerPruebas.repositories.PocketRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * Clase que implementa la lógica de los bolsillos
 */
@Slf4j
@Service
public class PocketService {

    @Autowired
    private PocketRepository pocketRepository;

    @Autowired
    private AccountRepository accountRepository;

    /**
     * Crea un bolsillo para una cuenta.
     * 
     * @param accountId id de la cuenta a la cual se le va a crear el bolsillo
     * @param pocketEntity entidad del bolsillo a crear
     * @return entidad del bolsillo creado
     * @throws EntityNotFoundException si la cuenta no existe
     * @throws BusinessLogicException si la cuenta está bloqueada o si ya existe un bolsillo con el mismo nombre en la cuenta
     */
    @Transactional
    public PocketEntity createPocket(Long accountId, PocketEntity pocketEntity) throws EntityNotFoundException, BusinessLogicException {
        log.info("Inicia proceso de creación de un bolsillo para la cuenta con id = {}", accountId);
        
        // 1. Verificar que la cuenta existe
        Optional<AccountEntity> accountEntity = accountRepository.findById(accountId);
        if (accountEntity.isEmpty()) {
            throw new EntityNotFoundException("La cuenta no existe");
        }

        // 2. Verificar que la cuenta esté activa
        if (!"ACTIVA".equals(accountEntity.get().getEstado())) {
            throw new BusinessLogicException("La cuenta debe estar en estado ACTIVA para crear bolsillos");
        }

        // 3. Verificar que no exista un bolsillo con el mismo nombre en esa cuenta
        for (PocketEntity p : accountEntity.get().getPockets()) {
            if (p.getNombre().equals(pocketEntity.getNombre())) {
                throw new BusinessLogicException("Ya existe un bolsillo con el mismo nombre en esta cuenta");
            }
        }

        // 4. Asociar el bolsillo a la cuenta y guardar
        pocketEntity.setAccount(accountEntity.get());
        log.info("Termina proceso de creación de un bolsillo para la cuenta con id = {}", accountId);
        return pocketRepository.save(pocketEntity);
    }


    @Transactional
    public void MoveToPocket(Long accountId, Long pocketId, Double amount) throws EntityNotFoundException, BusinessLogicException {
        log.info("Inicia proceso de mover dinero a un bolsillo para la cuenta con id = {} y bolsillo con id = {}", accountId, pocketId);
        
        // 1. Verificar que saldo de cuenta es mayor al monto que se va a mover y que la cuenta existe
        Optional<AccountEntity> accountEntity = accountRepository.findById(accountId);
        if (accountEntity.isEmpty()) {
            throw new EntityNotFoundException("La cuenta no existe");

        }
        if (accountEntity.get().getSaldo() < amount) {
            throw new BusinessLogicException("La cuenta no tiene suficiente saldo para mover a un bolsillo");
        }

        // 2. Restar el monto a mover del saldo de la cuenta
        accountEntity.get().setSaldo(accountEntity.get().getSaldo() - amount);
        accountRepository.save(accountEntity.get());

        // 3. Sumar el monto a mover al saldo del bolsillo
        Optional<PocketEntity> pocketEntity = pocketRepository.findById(pocketId);
        
        pocketEntity.get().setSaldo(pocketEntity.get().getSaldo() + amount);
        pocketRepository.save(pocketEntity.get());

    }
}
