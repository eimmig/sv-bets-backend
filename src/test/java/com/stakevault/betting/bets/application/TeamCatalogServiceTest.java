package com.stakevault.betting.bets.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.stakevault.betting.bets.domain.model.CatalogAlreadyRegisteredException;
import com.stakevault.betting.bets.domain.model.SportNotFoundException;
import com.stakevault.betting.bets.domain.model.Team;
import com.stakevault.betting.bets.domain.port.out.SportRepository;
import com.stakevault.betting.bets.domain.port.out.TeamRepository;

@ExtendWith(MockitoExtension.class)
class TeamCatalogServiceTest {

	@Mock
	private TeamRepository teamRepository;
	@Mock
	private SportRepository sportRepository;

	private TeamCatalogService service() {
		return new TeamCatalogService(teamRepository, sportRepository);
	}

	@Test
	void shouldCreateTeamWhenSportExistsAndNameIsUnusedForThatSport() {
		UUID sportId = UUID.randomUUID();
		when(sportRepository.existsById(sportId)).thenReturn(true);
		when(teamRepository.existsByNameAndSportId("Furia", sportId)).thenReturn(false);
		when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Team team = service().create("Furia", sportId);

		assertThat(team.name()).isEqualTo("Furia");
		assertThat(team.sportId()).isEqualTo(sportId);
	}

	@Test
	void shouldRejectWhenSportDoesNotExist() {
		UUID sportId = UUID.randomUUID();
		when(sportRepository.existsById(sportId)).thenReturn(false);
		TeamCatalogService service = service();

		assertThatThrownBy(() -> service.create("Furia", sportId)).isInstanceOf(SportNotFoundException.class);
	}

	@Test
	void shouldRejectWhenNameAlreadyRegisteredForSameSport() {
		UUID sportId = UUID.randomUUID();
		when(sportRepository.existsById(sportId)).thenReturn(true);
		when(teamRepository.existsByNameAndSportId("Furia", sportId)).thenReturn(true);
		TeamCatalogService service = service();

		assertThatThrownBy(() -> service.create("Furia", sportId))
				.isInstanceOf(CatalogAlreadyRegisteredException.class);
	}

	@Test
	void shouldRejectOnConcurrentUniqueConstraintRace() {
		UUID sportId = UUID.randomUUID();
		when(sportRepository.existsById(sportId)).thenReturn(true);
		when(teamRepository.existsByNameAndSportId("Furia", sportId)).thenReturn(false);
		when(teamRepository.save(any(Team.class))).thenThrow(new DataIntegrityViolationException("conflict"));
		TeamCatalogService service = service();

		assertThatThrownBy(() -> service.create("Furia", sportId))
				.isInstanceOf(CatalogAlreadyRegisteredException.class);
	}
}
