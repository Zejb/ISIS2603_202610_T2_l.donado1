package co.edu.uniandes.dse.TallerPruebas.services;

import co.edu.uniandes.dse.TallerPruebas.exceptions.BusinessLogicException;
import co.edu.uniandes.dse.TallerPruebas.exceptions.EntityNotFoundException;
import co.edu.uniandes.dse.TallerPruebas.repositories.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Slf4j
@Service

public class TransactionService {

    @Autowired
    private AccountRepository accountRepository;



    @Transactional
    public void createTransaction( Long accountId1, Long accountId2, Double amount) throws EntityNotFoundException, BusinessLogicException{

        // 1. Verificar que las cuentas existan

        if(accountRepository.findById(accountId1).isEmpty() || accountRepository.findById(accountId2).isEmpty()){
            throw new EntityNotFoundException("Una de las cuentas no existe");
        }

        // 2. Verificar que las cuentas sean diferentes
        if(accountId1.equals(accountId2)){
            throw new BusinessLogicException("Las cuentas deben ser diferentes");
        }

        // 3. Verificar que la cuenta 1 tenga suficiente saldo
        if(accountRepository.findById(accountId1).get().getSaldo() < amount){
            throw new BusinessLogicException("La cuenta 1 no tiene suficiente saldo");
        }
    }
}