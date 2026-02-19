package org.SportsIn.services;

import org.SportsIn.model.chat.Message;
import org.SportsIn.model.user.Equipe;
import org.SportsIn.model.user.Joueur;
import org.SportsIn.repository.EquipeRepository;
import org.SportsIn.repository.JoueurRepository;
import org.SportsIn.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final JoueurRepository joueurRepository;
    private final EquipeRepository equipeRepository;

    public MessageService(MessageRepository messageRepository,
                          JoueurRepository joueurRepository,
                          EquipeRepository equipeRepository) {
        this.messageRepository = messageRepository;
        this.joueurRepository = joueurRepository;
        this.equipeRepository = equipeRepository;
    }

    public List<Message> getByEquipe(Long equipeId) {
        return messageRepository.findByEquipe_IdOrderByEnvoyeAAsc(equipeId);
    }

    public Optional<Message> send(Long joueurId, Long equipeId, String contenu) {
        Optional<Joueur> joueurOpt = joueurRepository.findById(joueurId);
        Optional<Equipe> equipeOpt = equipeRepository.findById(equipeId);

        if (joueurOpt.isEmpty() || equipeOpt.isEmpty()) {
            return Optional.empty();
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Message message = new Message(contenu, joueurOpt.get(), equipeOpt.get(), now);
        return Optional.of(messageRepository.save(message));
    }

    public boolean delete(Long messageId) {
        if (messageRepository.existsById(messageId)) {
            messageRepository.deleteById(messageId);
            return true;
        }
        return false;
    }
}
