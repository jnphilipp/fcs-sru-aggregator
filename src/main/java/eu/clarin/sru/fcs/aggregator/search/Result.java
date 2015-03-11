package eu.clarin.sru.fcs.aggregator.search;

import eu.clarin.sru.client.SRUDiagnostic;
import eu.clarin.sru.client.SRURecord;
import eu.clarin.sru.client.SRUSearchRetrieveRequest;
import eu.clarin.sru.fcs.aggregator.scan.Corpus;
import eu.clarin.sru.client.SRUSearchRetrieveResponse;
import eu.clarin.sru.client.SRUSurrogateRecordData;
import eu.clarin.sru.client.fcs.ClarinFCSRecordData;
import eu.clarin.sru.client.fcs.DataView;
import eu.clarin.sru.client.fcs.DataViewGenericDOM;
import eu.clarin.sru.client.fcs.DataViewGenericString;
import eu.clarin.sru.client.fcs.DataViewHits;
import eu.clarin.sru.client.fcs.Resource;
import eu.clarin.sru.fcs.aggregator.scan.Diagnostic;
import eu.clarin.sru.fcs.aggregator.scan.JsonException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.w3c.dom.Node;
import org.slf4j.LoggerFactory;

/**
 * Represents the results of a SRU search-retrieve operation request. It
 * contains the endpoint/corpus (if specified in the request) to which a request
 * was sent, and the corresponding SRU search-retrieve response.
 *
 * @author Yana Panchenko
 * @author edima
 */
public final class Result {

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(Result.class);

	private final Corpus corpus;
	private AtomicBoolean inProgress = new AtomicBoolean(true);
	private AtomicInteger endpointReturnedRecords = new AtomicInteger();
	private AtomicReference<JsonException> exception = new AtomicReference<JsonException>();
	private List<Diagnostic> diagnostics = Collections.synchronizedList(new ArrayList<Diagnostic>());
	private List<Kwic> kwics = Collections.synchronizedList(new ArrayList<Kwic>());

	public List<Kwic> getKwics() {
		return kwics;
	}

	public Result(Corpus corpus) {
		this.corpus = corpus;
	}

	public void setInProgress(boolean inProgress) {
		this.inProgress.set(inProgress);
	}

	public boolean getInProgress() {
		return inProgress.get();
	}

	public void addResponse(SRUSearchRetrieveResponse response) {
		if (response != null && response.hasRecords()) {
			for (SRURecord record : response.getRecords()) {
				addRecord(record);
			}
		}
		if (response != null && response.hasDiagnostics()) {
			for (SRUDiagnostic d : response.getDiagnostics()) {
				diagnostics.add(new Diagnostic(d.getURI(), d.getMessage(), d.getDetails()));
			}
		}
	}

	void addRecord(SRURecord record) {
//		TODO(edima): use response.getNextRecordPosition()
		endpointReturnedRecords.getAndIncrement();
		if (record.isRecordSchema(ClarinFCSRecordData.RECORD_SCHEMA)) {
			ClarinFCSRecordData rd = (ClarinFCSRecordData) record.getRecordData();
			Resource resource = rd.getResource();
			setClarinRecord(resource);
			log.debug("Resource ref={0}, pid={1}, dataViews={2}",
					new Object[]{resource.getRef(), resource.getPid(), resource.hasDataViews()});
		} else if (record.isRecordSchema(SRUSurrogateRecordData.RECORD_SCHEMA)) {
			SRUSurrogateRecordData r = (SRUSurrogateRecordData) record.getRecordData();
			log.info("Surrogate diagnostic: uri={0}, message={1}, detail={2}",
					new Object[]{r.getURI(), r.getMessage(), r.getDetails()});
		} else {
			log.info("Unsupported schema: {0}", record.getRecordSchema());
		}
	}

	private void setClarinRecord(Resource resource) {
		String pid = resource.getPid();
		String reference = resource.getRef();

		if (resource.hasDataViews()) {
			processDataViews(resource.getDataViews(), pid, reference);
		}

		if (resource.hasResourceFragments()) {
			for (Resource.ResourceFragment fragment : resource.getResourceFragments()) {
				log.debug("ResourceFragment: ref={0}, pid={1}, dataViews={2}",
						new Object[]{fragment.getRef(), fragment.getPid(), fragment.hasDataViews()});
				if (fragment.hasDataViews()) {
					processDataViews(fragment.getDataViews(),
							fragment.getPid() != null ? fragment.getPid() : pid,
							fragment.getRef() != null ? fragment.getRef() : reference);
				}
			}
		}
	}

	private void processDataViews(List<DataView> dataViews, String pid, String reference) {
		for (DataView dataview : dataViews) {
			if (dataview instanceof DataViewGenericDOM) {
				final DataViewGenericDOM view = (DataViewGenericDOM) dataview;
				final Node root = view.getDocument().getFirstChild();
				log.debug("DataView (generic dom): root element <{}> / {}",
						root.getNodeName(),
						root.getOwnerDocument().hashCode());
			} else if (dataview instanceof DataViewGenericString) {
				final DataViewGenericString view
						= (DataViewGenericString) dataview;
				log.debug("DataView (generic string): data = {}",
						view.getContent());
			} else if (dataview instanceof DataViewHits) {
				final DataViewHits hits = (DataViewHits) dataview;
				Kwic kwic = new Kwic(hits, pid, reference);
				kwics.add(kwic);
				log.debug("DataViewHits: {}", kwic.getFragments());
			}
		}
	}

	public List<Diagnostic> getDiagnostics() {
		return Collections.unmodifiableList(diagnostics);
	}

	public JsonException getException() {
		return exception.get();
	}

	public void setException(Exception xc) {
		exception.set(new JsonException(xc));
	}

	public int getEndpointReturnedRecords() {
		return endpointReturnedRecords.get();
	}

	public Corpus getCorpus() {
		return corpus;
	}
}
