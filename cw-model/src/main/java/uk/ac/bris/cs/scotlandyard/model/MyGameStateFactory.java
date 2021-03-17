package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.swing.text.html.Option;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.*;

import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.DOUBLE;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.SECRET;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {

	private final class MyVisitor implements Move.Visitor<Player> {
		private Player player;
		private int destionation;

		public MyVisitor(Player player) {
			this.player = player;
		}

		@Override
		public Player visit(Move.SingleMove move) {
			this.destionation = move.destination;
			return player.at(move.destination);
		}

		@Override
		public Player visit(Move.DoubleMove move) {
			this.destionation = move.destination2;
			return player.at(move.destination2);
		}

		public int getDestionation() {
			return this.destionation;
		}
	}

	private final class MyTicketBoard implements Board.TicketBoard {
		private ImmutableMap<ScotlandYard.Ticket, Integer> tickets;
		MyTicketBoard(ImmutableMap<ScotlandYard.Ticket, Integer> tickets) {
			this.tickets = tickets;
		}
		@Override
		public int getCount(@Nonnull ScotlandYard.Ticket ticket) {
			return this.tickets.get(ticket);
		}
	}

	private final class MyGameState implements GameState{
		private GameSetup setup;
		private ImmutableSet<Piece> remaining;
		private ImmutableList<LogEntry> log;
		private Player mrX;
		private List<Player> detectives;
		private ImmutableMap<Piece, Boolean> played;
		private ImmutableList<Player> everyone;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;
		private int currentRound, currentPlayer;
		// current 1 ~ size
		// currentPlayer: 0 mrX, 1 others

		// whose term

		// setter
		public void setSetup(GameSetup setup) {
			this.setup = setup;
		}

		public void setRemaining(ImmutableSet<Piece> remaining) {
			this.remaining = remaining;
		}

		public void setLog(ImmutableList<LogEntry> log) {
			this.log = log;
		}

		public void setMrX(Player mrX) {
			this.mrX = mrX;
		}

		public void setDetectives(List<Player> detectives) {
			this.detectives = detectives;
		}

		public void setEveryone(ImmutableList<Player> everyone) {
			this.everyone = everyone;
		}

		public void setMoves(ImmutableSet<Move> moves) {
			this.moves = moves;
		}

		public void setWinner(ImmutableSet<Piece> winner) {
			this.winner = winner;
		}

		private int getCurrentRound() {
			return this.currentRound;
		}

		private int getLeftRounds() {
			return setup.rounds.size() - getCurrentRound();
		}

		private Map<Player, Boolean> generateWhoPlayed() {
			return null;
		}

		// constructor
		private MyGameState(final MyGameState old_state) {
			this.setup = old_state.setup;
			this.remaining = old_state.remaining;
			this.log = old_state.log;
			this.mrX = old_state.mrX;
			this.detectives = old_state.detectives;
			this.played = old_state.played;
			this.everyone = old_state.everyone;
			this.moves = old_state.moves;
			this.winner = old_state.winner;
			this.currentPlayer = old_state.currentPlayer;
			this.currentRound = old_state.currentRound;
		}

		private MyGameState(final GameSetup setup,
							final ImmutableSet<Piece> remaining, final ImmutableList<LogEntry> log,
							final Player mrX, final List<Player> detectives) {

			// Test Null, throw NullPointerException
			if (setup == null) throw new NullPointerException();
			if (mrX == null) throw new NullPointerException();
			if (detectives == null) throw new NullPointerException();
			for (Player player: detectives) {
				if (player == null) throw new NullPointerException();
			}
			// Test Illegal, IllegalArgumentException
			int mrXCount = 0;
			if (mrX.piece() == MRX) mrXCount++;
			for (Player player: detectives) {
				if (player.piece() == MRX) mrXCount++;
			}
			if (mrXCount != 1) throw new IllegalArgumentException();
			// Has one mrX, but the first player isn't mrX
			if (mrX.piece() != MRX) throw new IllegalArgumentException();
			// Duplicate detectives
			for (int i = 0; i < detectives.size() - 1; i++)
				for (int j = i + 1; j < detectives.size(); j++)
					if (detectives.get(i).piece().equals(detectives.get(j).piece()))
						throw new IllegalArgumentException();
			// overlap
			for (int i = 0; i < detectives.size() - 1; i++)
				for (int j = i + 1; j < detectives.size(); j++)
					if (detectives.get(i).location() == detectives.get(j).location())
						throw new IllegalArgumentException();
			// detective has secret tickets, double
			for (Player player: detectives) {
				if (player.has(SECRET)) throw new IllegalArgumentException();
				if (player.has(DOUBLE)) throw new IllegalArgumentException();
			}
			// testEmptyRoundsShouldThrow
			if (setup.rounds.size() == 0) throw new IllegalArgumentException();
			// testEmptyGraphShouldThrow
			if (setup.graph.nodes().size() == 0) throw new IllegalArgumentException();

			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;
			this.moves = ImmutableSet.of();
			this.currentPlayer = 0;
			this.currentRound = 0;
			Map<Piece, Boolean> whoPlayed = new HashMap<Piece, Boolean>();
			whoPlayed.put(this.mrX.piece(), false);
			for (final var p : this.detectives) {
				whoPlayed.put(p.piece(), false);
			}
			this.played = ImmutableMap.copyOf(whoPlayed);
		}

		@Nonnull
		@Override
		public GameSetup getSetup() {
			return setup;
		}

		@Nonnull
		@Override
		public ImmutableSet<Piece> getPlayers() {
			ImmutableSet<Piece> ret = ImmutableSet.of(mrX.piece());
			for (final var p : detectives) {
				ret = ImmutableSet.<Piece>builder().addAll(ret).add(p.piece()).build();
			}
			return ret;
		}

		@Nonnull
		@Override
		public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
			for (final var p : detectives) {
				if (p.piece() == detective) return Optional.of(p.location());
			}
			return Optional.empty();
		}

		@Nonnull
		@Override
		public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			if (mrX.piece() == piece) return Optional.of(new MyTicketBoard(mrX.tickets()));
			for (final var p: detectives) {
				if (p.piece() == piece) return Optional.of(new MyTicketBoard(p.tickets()));
			}
			return Optional.empty();
		}

		@Nonnull
		@Override
		public ImmutableList<LogEntry> getMrXTravelLog() {
			return log;
		}

		@Nonnull
		@Override
		public ImmutableSet<Piece> getWinner() {
			ImmutableSet<Piece> ret = ImmutableSet.of();

			// if mrX is cornered
			Boolean allDestinationsHaveDetectives = false;
			if (getValidMoves(this.setup, this.detectives, this.mrX, this.mrX.location()).isEmpty()) {
				allDestinationsHaveDetectives = true;
			}

			// if detectives is cornered
			// no detectives can move, and during this round no detective has moved.
			Integer canMoveDetectivesCount = 0;
			Integer movedDetectives = 0;
			for (final var p : detectives) {
				if (this.played.get(p.piece()).equals(true)) {
					movedDetectives ++;
					continue;
				}
				if (!getValidMoves(this.setup, this.detectives, p, p.location()).isEmpty()) {
					canMoveDetectivesCount ++;
				}
			}

			// if mrX is covered by detectives
			Boolean mrXCoveredByDetectives = false;
			for (final var p : detectives) {
				if (p.location() == mrX.location()) {
					mrXCoveredByDetectives = true;
				}
			}

			Set<Piece> mrXWin = new HashSet<Piece>();
			mrXWin.add(mrX.piece());
			Set<Piece> detectivesWin = new HashSet<Piece>();
			for (final var p : detectives) {
				detectivesWin.add(p.piece());
			}

			if (this.getLeftRounds() == 0 && this.currentPlayer == 0 && !mrXCoveredByDetectives) {
				// mrX win
				return ImmutableSet.copyOf(mrXWin);
			}

			if (this.currentPlayer == 0 && allDestinationsHaveDetectives && this.getLeftRounds() > 0) {
				// mrX cannot move, lose
				return ImmutableSet.copyOf(detectivesWin);
			}

			if (mrXCoveredByDetectives) {
				// mrX lose
				return ImmutableSet.copyOf(detectivesWin);
			}

			if (canMoveDetectivesCount == 0 && movedDetectives == 0) {
				// detectives lose
				return ImmutableSet.copyOf(mrXWin);
			}

			return ImmutableSet.of();
		}

		private boolean destinationHasPlayer(List<Player> players, int destination) {
			for (final var p: players) {
				if (p.location() == destination)
					return true;
			}
			return false;
		}

		// single move
		private ImmutableSet<Move.SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {
			final var singleMoves = new ArrayList<Move.SingleMove>();
			for (int destination: setup.graph.adjacentNodes(source)) {
				if (destinationHasPlayer(detectives, destination)) continue;
				for (ScotlandYard.Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of())) {
					if (player.has(t.requiredTicket()))
						singleMoves.add(new Move.SingleMove(player.piece(), source, t.requiredTicket() , destination));
					// secret ticket
					if (player.has(SECRET))
						singleMoves.add(new Move.SingleMove(player.piece(), source, SECRET, destination));
				}
			}
			return ImmutableSet.copyOf(singleMoves);
		}

		// double move
		private ImmutableSet<Move.DoubleMove> makeDoubleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {
			final var doubleMoves = new ArrayList<Move.DoubleMove>();
			if (!player.has(DOUBLE)) return ImmutableSet.of();
			if (getLeftRounds() < 2) return ImmutableSet.of();
			for (int destination1 : setup.graph.adjacentNodes(source)) {
				if (destinationHasPlayer(detectives, destination1)) continue;
				for (int destination2 : setup.graph.adjacentNodes(destination1)) {
					if (destinationHasPlayer(detectives, destination2)) continue;
					for (ScotlandYard.Transport t1 : setup.graph.edgeValueOrDefault(source, destination1, ImmutableSet.of())) {
						for (ScotlandYard.Transport t2 : setup.graph.edgeValueOrDefault(destination1, destination2, ImmutableSet.of())) {
							// destination1, 2 empty, no detectives
							ScotlandYard.Ticket ticket1 = t1.requiredTicket(), ticket2 = t2.requiredTicket();
							if (ticket1.equals(ticket2)) {
								if (player.hasAtLeast(ticket1, 2)) {
									doubleMoves.add(new Move.DoubleMove(player.piece(), source, ticket1, destination1, ticket2, destination2));
								}
								if (player.has(ticket1) && player.has(SECRET)) {
									doubleMoves.add(new Move.DoubleMove(player.piece(), source, ticket1, destination1, SECRET, destination2));
								}
								if (player.has(ticket2) && player.has(SECRET)) {
									doubleMoves.add(new Move.DoubleMove(player.piece(), source, SECRET, destination1, ticket2, destination2));
								}
								if (player.hasAtLeast(SECRET, 2)) {
									doubleMoves.add(new Move.DoubleMove(player.piece(), source, SECRET, destination1, SECRET, destination2));
								}
							} else {
								if (player.has(ticket1) && player.has(ticket2)) {
									doubleMoves.add(new Move.DoubleMove(player.piece(), source, ticket1, destination1, ticket2, destination2));
								}
								if (player.has(SECRET) && player.has(ticket2)) {
									doubleMoves.add(new Move.DoubleMove(player.piece(), source, SECRET, destination1, ticket2, destination2));
								}
								if (player.has(ticket1) && player.has(SECRET)) {
									doubleMoves.add(new Move.DoubleMove(player.piece(), source, ticket1, destination1, SECRET, destination2));
								}
								if (player.hasAtLeast(SECRET, 2)) {
									doubleMoves.add(new Move.DoubleMove(player.piece(), source, SECRET, destination1, SECRET, destination2));
								}
							}
						}
					}
				}
			}
			return ImmutableSet.copyOf(doubleMoves);
		}

		private ImmutableSet<Move> getValidMoves(GameSetup setup, List<Player> detectives, Player player, int source) {
			ImmutableSet<Move> ret = ImmutableSet.of();
			ret = ImmutableSet.<Move>builder().addAll(ret).addAll(makeSingleMoves(setup, detectives, player, player.location())).build();
			ret = ImmutableSet.<Move>builder().addAll(ret).addAll(makeDoubleMoves(setup, detectives, player, player.location())).build();
			return ret;
		}

		@Nonnull
		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			if (!getWinner().isEmpty()) return ImmutableSet.of();
			ImmutableSet<Move> ret = ImmutableSet.of();
			if (currentPlayer == 0) {
				ret = ImmutableSet.<Move>builder().addAll(ret).addAll(getValidMoves(setup, detectives, mrX, mrX.location())).build();
			} else {
				for (final var p: detectives) {
					if (this.played.get(p.piece()).equals(true)) continue;
					ret = ImmutableSet.<Move>builder().addAll(ret).addAll(getValidMoves(setup, detectives, p, p.location())).build();
				}
			}
			return ret;
		}

		@Nonnull
		@Override
		public GameState advance(Move move) {
			if (moves.contains(move)) throw new IllegalArgumentException("Illegal Move: " + move);

			if (!getAvailableMoves().contains(move)) throw new IllegalArgumentException("Illegal Move: " + move);

			// return a new state copy of now
			MyGameState ret = new MyGameState(this);

			if (ret.currentPlayer == 0) {

				Map<Piece, Boolean> whoPlayed = new HashMap<Piece, Boolean>(ret.played);

				// use
				ret.mrX = ret.mrX.use(move.tickets());
				ret.mrX = move.visit(new MyVisitor(ret.mrX));

				whoPlayed.put(ret.mrX.piece(), true);
				ret.played = ImmutableMap.copyOf(whoPlayed);
				List<LogEntry> mrXlog = new ArrayList<LogEntry>(ret.log);

				int deltaRound = 0;
				for (final var m : move.tickets()) {
					if (m.equals(DOUBLE)) continue;
					if (ret.setup.rounds.get(ret.currentRound + deltaRound)) {
						mrXlog.add(LogEntry.reveal(m, ret.mrX.location()));
					} else {
						mrXlog.add(LogEntry.hidden(m));
					}
					deltaRound ++;
				}

				ret.log = ImmutableList.copyOf(mrXlog);

				ret.currentPlayer = 1;
			} else {

				Map<Piece, Boolean> whoPlayed = new HashMap<Piece, Boolean>(ret.played);

				List<Player> tmp = new ArrayList<>();
				for (final var p : ret.detectives) {
					if (p.piece() == move.commencedBy()) {

						Player tmp_p = p;
						tmp_p = tmp_p.use(move.tickets());
						tmp_p = move.visit(new MyVisitor(tmp_p));

						whoPlayed.put(tmp_p.piece(), true);
						tmp.add(tmp_p);
						ret.mrX = ret.mrX.give(move.tickets());
					} else {
						tmp.add(p);
					}
				}
				ret.detectives = ImmutableList.copyOf(tmp);
				ret.played = ImmutableMap.copyOf(whoPlayed);

				//
				Integer playedCount = 0;
				Integer cannotMoveCount = 0;
				for (final var p : ret.detectives) {
					if (ret.played.get(p.piece()).equals(true)) {
						playedCount++;
					} else if (ret.played.get(p.piece()).equals(false) && getValidMoves(ret.setup, ret.detectives, p, p.location()).isEmpty()) {
						cannotMoveCount++;
					}
				}

				if (playedCount > 0 && (playedCount + cannotMoveCount == ret.detectives.size())) {
					ret.currentPlayer = 0;
					ret.currentRound ++;
					whoPlayed = new HashMap<Piece, Boolean>();
					whoPlayed.put(ret.mrX.piece(), false);
					for (final var p : ret.detectives) {
						whoPlayed.put(p.piece(), false);
					}
					ret.played = ImmutableMap.copyOf(whoPlayed);
				}
			}
			return ret;
		}
	}

	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {
		// TODO
		return new MyGameState(setup, ImmutableSet.of(MRX), ImmutableList.of(), mrX, detectives);
	}

}
