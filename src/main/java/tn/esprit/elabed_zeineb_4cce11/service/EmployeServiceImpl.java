package tn.esprit.elabed_zeineb_4cce11.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.elabed_zeineb_4cce11.domain.Employe;
import tn.esprit.elabed_zeineb_4cce11.exception.ResourceNotFoundException;
import tn.esprit.elabed_zeineb_4cce11.repository.IEmployeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeServiceImpl implements IEmployeService {

    private final IEmployeRepository employeRepository;

    @Override
    public Employe create(Employe employe) {
        if (employe.getIdEmploye() != null) {
            throw new IllegalArgumentException("Un nouvel employé ne doit pas avoir d'identifiant");
        }
        validerAttributsObligatoires(employe);
        return employeRepository.save(employe);
    }

    @Override
    public Employe findById(Long id) {
        return employeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employe", id));
    }

    @Override
    public List<Employe> findAll() {
        return employeRepository.findAll();
    }

    @Override
    public Employe update(Long id, Employe employe) {
        Employe existant = findById(id);
        validerAttributsObligatoires(employe);
        existant.setNom(employe.getNom());
        existant.setPrenom(employe.getPrenom());
        existant.setRole(employe.getRole());
        existant.setAgence(employe.getAgence());
        return employeRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!employeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employe", id);
        }
        employeRepository.deleteById(id);
    }

    private void validerAttributsObligatoires(Employe employe) {
        if (employe.getNom() == null || employe.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de l'employé est obligatoire et ne peut pas être vide");
        }
        if (employe.getPrenom() == null || employe.getPrenom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le prénom de l'employé est obligatoire et ne peut pas être vide");
        }
        if (employe.getRole() == null) {
            throw new IllegalArgumentException("Le rôle/poste de l'employé est obligatoire");
        }
    }
}
