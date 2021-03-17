package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;
import javax.swing.text.html.Option;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import uk.ac.bris.cs.scotlandyard.model.*;

public class MyAi implements Ai {
	// graph
	private ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> gameGraph;
	// mrX's last location
	private int mrXLastLocation = 0;

	// coefficient
	// mrX's score = ca * validMovesCount + cb * distance
	// detectives's score = ca * validMovesCount - cb * distance
	private double ca = 5.0;
	private double cb = 10.0;

	@Nonnull @Override public String name() { return "myAI"; }

	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			@Nonnull AtomicBoolean terminate) {
		// returns a random move, replace with your own implementation
		var moves = board.getAvailableMoves().asList();
		var move = moves.get(0);
		// init
		gameGraph = board.getSetup().graph;

		// update mrX's last location
		var travelLog = board.getMrXTravelLog();
		for (int i = travelLog.size() - 1; i >= 0; i--) {
			if (travelLog.get(i).location().equals(Optional.empty())) {
				mrXLastLocation = -1;
			} else {
				mrXLastLocation = travelLog.get(i).location().get().intValue();
				break;
			}
		}

		Double maxScore = Double.MIN_VALUE;
		int maxScoreIndex = 0;
		if (moves.get(0).commencedBy().isMrX()) {
			// mrX
			for (int i = 0; i < moves.size(); i++) {
				final var m  = moves.get(i);
				Double tmpScore = 0.0;
				if (m instanceof Move.SingleMove) {
					tmpScore = scoremrX(((Move.SingleMove) m).destination, board.getPlayers(), board) * cb + ca * moves.size();
				} else if (m instanceof Move.DoubleMove) {
					tmpScore = scoremrX(((Move.DoubleMove) m).destination2, board.getPlayers(), board) * cb + ca * moves.size();
				}
				if (maxScore.compareTo(tmpScore) == -1) {
					maxScore = tmpScore;
					maxScoreIndex = i;
				}
			}
		} else {
			// detectives
			for (int i = 0; i < moves.size(); i++) {
				final var m = moves.get(i);
				Double tmpScore = 0.0;
				int validMovesCount = 0;
				for (final var im : moves) {
					if (im.commencedBy() == m.commencedBy()) {
						validMovesCount ++;
					}
				}
				if (m instanceof Move.SingleMove) {
					tmpScore = -scoreDetectives(((Move.SingleMove) m).destination, this.mrXLastLocation, board) * cb + ca * validMovesCount;
				} else if (m instanceof Move.DoubleMove) {
					tmpScore = -scoreDetectives(((Move.DoubleMove) m).destination2, this.mrXLastLocation, board) * cb + ca * validMovesCount;
				}
				if (maxScore.compareTo(tmpScore) == -1) {
					maxScore = tmpScore;
					maxScoreIndex = i;
				}
			}
		}
		return moves.get(maxScoreIndex);
	}

	private Map<Integer, Integer> Dijkstra(int src, Board board) {
		Map<Integer, Integer> dist = new HashMap<>();
		Set<Integer> done = new HashSet<>();
		for (final var p : board.getSetup().graph.nodes()) {
			dist.put(p.intValue(), Integer.MAX_VALUE);
		}
		dist.put(src, 0);
		// dijkstra loop
		for (int i = 0; i < board.getSetup().graph.nodes().size(); i++) {
			Integer x = null, m = Integer.MAX_VALUE;
			for (final var p : board.getSetup().graph.nodes()) {
				if (!done.contains(p.intValue()) && (dist.get(p.intValue()).compareTo(m) <= 0)) {
					m = dist.get(p.intValue());
					x = p.intValue();
				}
			}
			if (x != null) {
				done.add(x);
				Integer dx = dist.get(x.intValue());
				for (final int y: board.getSetup().graph.adjacentNodes(x)) {
					Integer dy = dist.get(y);
					if (dy > dx + 1) {
						dist.put(y, dx + 1);
					}
				}
			}
		}
		return dist;
	}

	private Double scoreDetectives(int src, int mrXLastLocation, Board board) {
		Map<Integer, Integer> dist = Dijkstra(src, board);

		// the shortest short path
		Double ret = Double.MAX_VALUE;
		if (mrXLastLocation != -1) {
			ret = Double.min(ret, dist.get(mrXLastLocation));
		}
		return ret;
	}

	private Double scoremrX(int src, ImmutableSet<Piece> players, Board board) {
		Map<Integer, Integer> dist = Dijkstra(src, board);
		// min-max get the longest shortest distance from mrX's next move to detectives
		// the longest short path
		Double ret = Double.MAX_VALUE;
		for (final var p : board.getPlayers()) {
			if (p.isMrX()) continue;
			int pLocation = 0;
			if (!board.getDetectiveLocation((Piece.Detective) p).get().equals(Optional.empty()))
			 	pLocation = board.getDetectiveLocation((Piece.Detective) p).get().intValue();
			ret = Double.min(ret, dist.get(pLocation));
		}
		return ret;
	}

}