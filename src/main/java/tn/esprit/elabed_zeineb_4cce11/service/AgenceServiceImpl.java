package tn.esprit.elabed_zeineb_4cce11.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.elabed_zeineb_4cce11.domain.Agence;
import tn.esprit.elabed_zeineb_4cce11.exception.ResourceNotFoundException;
import tn.esprit.elabed_zeineb_4cce11.repository.IAgenceRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgenceServiceImpl implements IAgenceService {

    private final IAgenceRepository agenceRepository;

    @Override
    public Agence create(Agence agence) {
        if (agence.getIdAgence() != null) {
            throw new IllegalArgumentException("Une nouvelle agence ne doit pas avoir d'identifiant");
        }
        validerAttributsObligatoires(agence);
        return agenceRepository.save(agence);
    }

    @Override
    public Agence findById(Long id) {
        return agenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agence", id));
    }

    @Override
    public List<Agence> findAll() {
        return agenceRepository.findAll();
    }

    @Override
    public Agence update(Long id, Agence agence) {
        Agence existant = findById(id);
        validerAttributsObligatoires(agence);
        existant.setNom(agence.getNom());
        existant.setVille(agence.getVille());
        existant.setAdresse(agence.getAdresse());
        existant.setTelephone(agence.getTelephone());
        // sans toucher à l'identifiant ni aux collections vehicules / employes
        return agenceRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!agenceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Agence", id);
        }
        agenceRepository.deleteById(id);
    }

    private void validerAttributsObligatoires(Agence agence) {
        if (agence.getNom() == null || agence.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de l'agence est obligatoire et ne peut pas être vide");
        }
        if (agence.getVille() == null || agence.getVille().trim().isEmpty()) {
            throw new IllegalArgumentException("La ville de l'agence est obligatoire et ne peut pas être vide");
        }
    }
}
