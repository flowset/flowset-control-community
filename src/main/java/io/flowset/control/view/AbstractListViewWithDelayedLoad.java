package io.flowset.control.view;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.facet.urlqueryparameters.ControlPaginationUrlQueryParametersBinder;
import io.flowset.control.view.util.ComponentHelper;
import io.jmix.flowui.Facets;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.pagination.SimplePagination;
import io.jmix.flowui.facet.Timer;
import io.jmix.flowui.facet.UrlQueryParametersFacet;
import io.jmix.flowui.view.StandardListView;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.function.Supplier;

/**
 * An abstract base class for list views with a delayed data loading mechanism.
 * It extends <code>StandardListView</code> and incorporates features such as state management
 * for displaying the loading process, error handling, and UI updates.
 * This implementation adds an internal timer that loads data asynchronously.
 * <br/>
 * The following rules must be applied when implementing a view extending this view class:
 * <ol>
 *     <li>Data grid should contain an empty state component with id <code>emptyStateBox</code></li>
 *     <li>Add invocation of data loader that loads an item list showing in the data grid in the implementation of <code>loadData</code> method</li>
 *     <li>All actions triggering (e.g., refresh, filter applying) a data loading should call <code>startLoadData</code> method for async data loading</li>
 *     <li>Method loading data in the data loader delegate should be wrapped in the <code>loadItemsWithStateHandling</code> method invocation.</li>
 * </ol>
 *
 * @param <V> the type of the data being displayed in the list view
 */
public abstract class AbstractListViewWithDelayedLoad<V> extends StandardListView<V> {
    protected static final String GRID_EMPTY_CONTENT_DEFAULT_ID = "emptyStateBox";

    @Autowired
    protected ComponentHelper componentHelper;
    @Autowired
    protected UrlParamSerializer urlParamSerializer;

    protected boolean isLoading = false;
    protected String errorMessage;
    protected boolean hasData = false;
    protected boolean delayedLoadInProgress = false;

    protected Timer dataLoadTimer;
    protected VerticalLayout emptyStateBox;

    public AbstractListViewWithDelayedLoad() {
        addInitListener(this::onInitInternal);
        addBeforeShowListener(this::onBeforeShowInternal);
        addReadyListener(this::onReadyInternal);
    }

    private void onReadyInternal(ReadyEvent readyEvent) {
        if (!UiComponentUtils.isComponentAttachedToDialog(this)) {
            dataLoadTimer.start();
        } else {
            handleDataLoading(); // the timer is not started in case of dialog view
        }
    }

    private void onBeforeShowInternal(BeforeShowEvent beforeShowEvent) {
        isLoading = true;
        errorMessage = null;
        updateGridState();
    }

    private void onInitInternal(InitEvent initEvent) {
        Facets facets = getApplicationContext().getBean(Facets.class);
        dataLoadTimer = facets.create(Timer.class);
        dataLoadTimer.setId("dataLoadTimer");
        dataLoadTimer.setDelay(2);
        dataLoadTimer.setAutostart(false);
        dataLoadTimer.setRepeating(false);
        dataLoadTimer.addTimerActionListener(event -> handleDataLoading());
        getViewFacets().addFacet(dataLoadTimer);

        emptyStateBox = getContent().findComponent(GRID_EMPTY_CONTENT_DEFAULT_ID)
                .map(component -> (VerticalLayout) component)
                .orElseThrow(() -> new IllegalStateException("Unable to find empty grid component in %s by id %s"
                        .formatted(getClass(), GRID_EMPTY_CONTENT_DEFAULT_ID)));

    }

    /**
     * Registers a {@link ControlPaginationUrlQueryParametersBinder} for the given pagination component.
     * Use this method instead of the {@code pagination} element of the {@code urlQueryParameters} facet
     * in the view XML descriptor.
     *
     * @param paginationComponent a pagination component to bind
     */
    protected void registerPaginationParameterBinder(SimplePagination paginationComponent) {
        registerPaginationParameterBinder(paginationComponent, null, null, null);
    }

    /**
     * Same as {@link #registerPaginationParameterBinder(SimplePagination)}, but with a custom binder id
     * and URL query parameter names.
     *
     * @param paginationComponent  a pagination component to bind
     * @param binderId             an identifier of the binder
     * @param firstResultParamName a name of the URL query parameter for the first result
     * @param maxResultsParamName  a name of the URL query parameter for the max results
     */
    protected void registerPaginationParameterBinder(SimplePagination paginationComponent,
                                                     @Nullable String binderId,
                                                     @Nullable String firstResultParamName,
                                                     @Nullable String maxResultsParamName) {
        UrlQueryParametersFacet facet = getViewFacets()
                .getFacets()
                .filter(UrlQueryParametersFacet.class::isInstance)
                .map(UrlQueryParametersFacet.class::cast)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("%s facet is not found in %s"
                        .formatted(UrlQueryParametersFacet.NAME, getClass())));

        ControlPaginationUrlQueryParametersBinder binder = new ControlPaginationUrlQueryParametersBinder(
                paginationComponent, urlParamSerializer, this::isDelayedLoadInProgress);
        binder.setId(binderId);
        binder.setFirstResultParam(firstResultParamName);
        binder.setMaxResultsParam(maxResultsParamName);

        facet.registerBinder(binder);
        binder.saveInitialState();
    }

    /**
     * Loads data by timer action.
     */
    protected void handleDataLoading() {
        delayedLoadInProgress = true;
        try {
            loadData();
        } finally {
            delayedLoadInProgress = false;
        }
    }

    /**
     * @return {@code true} if a data loading initiated by the view itself, not by a user interaction,
     * is in progress
     */
    public boolean isDelayedLoadInProgress() {
        return delayedLoadInProgress;
    }

    /**
     * Provides a loading of the data showing in the data grid.
     */
    protected abstract void loadData();

    /**
     * Loads items using the provided supplier while managing the state of loading, error handling, and
     * updating the UI accordingly. Adjusts internal flags such as loading state, data presence,
     * and stores any error messages encountered during the process.
     *
     * @param itemsSupplier a supplier providing the list of items to be loaded
     * @return the list of loaded items if successful; or an empty list if an error occurs
     */
    protected List<V> loadItemsWithStateHandling(Supplier<List<V>> itemsSupplier) {
        try {
            List<V> data = itemsSupplier.get();
            hasData = CollectionUtils.isNotEmpty(data);
            return data;
        } catch (EngineConnectionFailedException e) {
            errorMessage = e.getMessage();
            hasData = false;
            return List.of();
        } finally {
            isLoading = false;
            updateGridState();
        }
    }

    /**
     * Updates the grid empty component depending on the current state of data loading:
     * <ul>
     *     <li>If data loading is in progress - shows a loading indicator</li>
     *     <li>If data is loaded but the list is empty - shows a "no data" component</li>
     *     <li>If data loading is completed, but an error occurs - shows an error message as an empty data grid component</li>
     *     <li>If data is loaded and the list is not empty - recalculate column widths in the data grid.</li>
     * </ul>
     */
    protected void updateGridState() {
        emptyStateBox.removeAll();
        emptyStateBox.addClassNames(LumoUtility.Gap.SMALL);

        if (isLoading) {
            componentHelper.addLoadingGridStateComponents(emptyStateBox);
        } else if (errorMessage != null) {
            componentHelper.addErrorStateGridStateComponents(emptyStateBox, errorMessage);
        } else if (!hasData) {
            componentHelper.addNoDataGridStateComponents(emptyStateBox);
        } else {
            emptyStateBox.getParent()
                    .ifPresent(component -> {
                        if (component instanceof DataGrid<?> dataGrid) {
                            dataGrid.recalculateColumnWidths();
                        }
                    });
        }
    }

    /**
     * Update states to "loading" and starts a time for loading data.
     */
    protected void startLoadData() {
        dataLoadTimer.stop();

        isLoading = true;
        errorMessage = null;
        updateGridState();

        dataLoadTimer.start();
    }
}
