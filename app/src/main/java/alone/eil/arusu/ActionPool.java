package alone.eil.arusu;

import java.util.HashMap;
import java.util.Map;

public class ActionPool {
	ActionPool() {}

	private final HashMap <String, Action> m_storage = new HashMap<>();

	public abstract class Action {
		public Boolean enabled;
		abstract void update(float dt);
	}

	public void add(Action what) {
		m_storage.put(what.getClass().getName(), what);
	}
	public void remove(Class <? extends Action> what) {
		m_storage.remove(what.getName());
	}

	public void update(float dt) {
		for (Map.Entry <String, Action> iter : m_storage.entrySet()) {
			Action action = iter.getValue();
			if (action.enabled)
				action.update(dt);
		}
	}
}
