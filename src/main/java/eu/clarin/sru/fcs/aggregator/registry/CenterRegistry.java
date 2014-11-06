package eu.clarin.sru.fcs.aggregator.registry;

import java.util.List;

/**
 * Interface representing starting point of FCS. For example,
 * center registry, that contains all the centers that in their turn contain
 * the information about supporting endpoints.
 *
 * @author Yana Panchenko
 */
public interface CenterRegistry {

    public boolean hasCQLInstitutionsLoaded();

    public void loadCQLInstitutions();
        
    public List<Institution> getCQLInstitutions();

	public Institution getCQLInstitution(int index);
}