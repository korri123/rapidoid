package org.rapidoid.db.impl;

/*
 * #%L
 * rapidoid-db
 * %%
 * Copyright (C) 2014 Nikolche Mihajlovski
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.rapidoid.db.Db;
import org.rapidoid.util.U;

import com.fasterxml.jackson.annotation.JsonValue;

public abstract class DbRelsCommons<E> implements DbRelationInternals {

	protected final Db db;

	protected final Object holder;

	protected final String relation;

	private final Collection<Long> ids;

	protected final DbRelChangesTracker tracker = new DbRelChangesTracker();

	public DbRelsCommons(Db db, Object holder, String relation, Collection<Long> ids) {
		this.db = db;
		this.holder = holder;
		this.relation = relation;
		this.ids = ids;
	}

	@Override
	public Set<Long> getAddedRelations() {
		return tracker.getAddedRelations();
	}

	@Override
	public Set<Long> getRemovedRelations() {
		return tracker.getRemovedRelations();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((db == null) ? 0 : db.hashCode());
		result = prime * result + ((ids == null) ? 0 : ids.hashCode());
		result = prime * result + ((relation == null) ? 0 : relation.hashCode());
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DbRelsCommons<E> other = (DbRelsCommons<E>) obj;
		if (db == null) {
			if (other.db != null)
				return false;
		} else if (!db.equals(other.db))
			return false;
		if (ids == null) {
			if (other.ids != null)
				return false;
		} else if (!ids.equals(other.ids))
			return false;
		if (relation == null) {
			if (other.relation != null)
				return false;
		} else if (!relation.equals(other.relation))
			return false;
		return true;
	}

	protected long getSingleId() {
		U.must(ids.size() <= 1);
		return !ids.isEmpty() ? ids.iterator().next() : -1;
	}

	@JsonValue
	public Object serialized() {
		return U.map("relation", relation, "ids", ids);
	}

	public boolean isEmpty() {
		return ids.isEmpty();
	}

	public int size() {
		return ids.size();
	}

	@Override
	public boolean hasId(long id) {
		return ids.contains(id);
	}

	private List<Long> getIdsAsList() {
		return (List<Long>) ids;
	}

	public Object[] toArray() {
		return db.getAll(ids).toArray();
	}

	public <T> T[] toArray(T[] arr) {
		return db.getAll(ids).toArray(arr);
	}

	protected List<E> records() {
		return db.<E> getAll(ids);
	}

	protected long getIdAt(int index) {
		return getIdsAsList().get(index);
	}

	protected int indexOfId(long id) {
		return getIdsAsList().indexOf(id);
	}

	protected int lastIndexOfId(long id) {
		return getIdsAsList().lastIndexOf(id);
	}

	protected List<Long> getIdSublist(int fromIndex, int toIndex) {
		// TODO make it modifiable (solve the problem of tracking changes through the sublist)
		return Collections.unmodifiableList(getIdsAsList().subList(fromIndex, toIndex));
	}

	/*
	 * THE FOLLOWING METHODS CHANGE THE IDs (IT IS IMPORTANT TO TRACK THE CHANGES):
	 */

	public void clear() {
		for (long id : ids) {
			tracker.removedRelTo(id);
		}
		ids.clear();
	}

	@Override
	public boolean addId(long id) {
		boolean changed = ids.add(id);

		if (changed) {
			tracker.addedRelTo(id);
		}

		return changed;
	}

	@Override
	public boolean removeId(long id) {
		boolean changed = ids.remove(id);

		if (changed) {
			tracker.removedRelTo(id);
		}

		return changed;
	}

	protected boolean retainIds(Collection<Long> retainIds) {
		for (Long id : ids) {
			if (!retainIds.contains(id)) {
				tracker.removedRelTo(id);
			}
		}

		return retainIds.retainAll(retainIds);
	}

	protected void addIdAt(int index, long id) {
		getIdsAsList().add(index, id);
		tracker.addedRelTo(id);
	}

	protected boolean addIdsAt(int index, Collection<Long> idsToAdd) {
		boolean changed = getIdsAsList().addAll(index, idsToAdd);

		for (Long id : idsToAdd) {
			tracker.addedRelTo(id);
		}

		return changed;
	}

	protected long removeIdAt(int index) {
		long removedId = getIdsAsList().remove(index);
		tracker.removedRelTo(removedId);
		return removedId;
	}

	protected long setIdAt(int index, long id) {
		long removedId = getIdsAsList().set(index, id);

		if (id != removedId) {
			tracker.addedRelTo(id);
			tracker.removedRelTo(removedId);
		}

		return removedId;
	}

}