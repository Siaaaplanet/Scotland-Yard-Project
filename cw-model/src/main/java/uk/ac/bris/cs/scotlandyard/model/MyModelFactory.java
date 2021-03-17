package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.*;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {

	private final class MyModel implements Model {

		Board.GameState state;
		private List<Observer> observers = new ArrayList<>();

		private MyModel(GameSetup setup, Player mrX, ImmutableList<Player> detectives) {
			this.state = new MyGameStateFactory().build(setup, mrX, detectives);
		}
		@Nonnull
		@Override
		public Board getCurrentBoard() {
			return this.state;
		}

		@Override
		public void registerObserver(@Nonnull Observer observer) {
			Objects.requireNonNull(observer);
			if (!this.observers.contains(observer)) {
				this.observers.add(observer);
			} else throw new IllegalArgumentException();
		}

		@Override
		public void unregisterObserver(@Nonnull Observer observer) {
			Objects.requireNonNull(observer);
			if (this.observers.contains(observer)) {
				this.observers.remove(observer);
			} else throw new IllegalArgumentException();
		}

		@Nonnull
		@Override
		public ImmutableSet<Observer> getObservers() {
			return ImmutableSet.copyOf(this.observers);
		}

		@Override
		public void chooseMove(@Nonnull Move move) {
			if (move == null) throw new NullPointerException();
			if (!this.state.getAvailableMoves().contains(move))
				throw new IllegalArgumentException();
			this.state = this.state.advance(move);
			var event = this.state.getWinner().isEmpty() ? Observer.Event.MOVE_MADE : Observer.Event.GAME_OVER;
			for (Observer o : this.observers) {
				o.onModelChanged(this.state, event);
			}
		}
	}

	@Nonnull @Override public Model build(GameSetup setup,
	                                      Player mrX,
	                                      ImmutableList<Player> detectives) {
		// TODO
		return new MyModel(setup, mrX, detectives);
	}
}
