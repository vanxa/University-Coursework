var pendingNotifications = 0;

function removeNotification( el )
{
	el.parentElement.removeChild( el );
	pendingNotifications--;
	updateNotifications();
}

function clearAllNotifications()
{
	el = document.getElementById('notificationsList');
	while( el.firstChild )
		removeNotification( el.firstChild );
}

function updateNotifications()
{
	ne = document.getElementById('notifications');
	ne.removeChild( ne.childNodes[0] );
	if( pendingNotifications <= 0 )
		t = document.createTextNode( 'No notifications'  );
	else
		t = document.createTextNode( pendingNotifications + ' notification' + (pendingNotifications > 1 ? 's' : '')  );
	ne.appendChild( t );
}

function addDropdownNotification(title,text,action)
{
	nele = document.getElementById('notifications');

	nlist = document.getElementById('notificationsList');

	nli = document.createElement('li');
	nli.appendChild( document.createTextNode(text) );

	nli.addEventListener( 'click' , action , true );

	nlist.appendChild( nli );
}

function checkNotify()
{
	if( window.webkitNotifications )
	{
		if( window.webkitNotifications.checkPermission() != 0 )
			window.webkitNotifications.requestPermission();
	}
}
		

function notify(title,text,action)
{
	pendingNotifications++;
	updateNotifications();

	addDropdownNotification(title,text,action);

	if( window.webkitNotifications )
	{
		if( window.webkitNotifications.checkPermission() == 0 )
		{
			var xn = window.webkitNotifications.createNotification( 'top.png' , title , text );
			xn.show();
			setTimeout( function() { xn.cancel() } , 7000 );
		} else {
			window.webkitNotifications.requestPermission();
		}
	}
}
