if ( !div.addEventListener && div.attachEvent && div.fireEvent ) {
	div.attachEvent( "onclick", clickFn = function() {
		// Cloning a node shouldn't copy over any
		// bound event handlers (IE does this)
		support.noCloneEvent = false;
	});
	div.cloneNode( true ).fireEvent("onclick");
	div.detachEvent( "onclick", clickFn );
}