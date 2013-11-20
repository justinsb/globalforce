var currentTask = null;

var queue = [];

var loading = false;

var taskHistory = [];

MIN_QUEUE_LENGTH = 5;
MAX_HISTORY = 100;

function setCurrentTask(task) {
	currentTask = task;
	if (currentTask != null) {
		$("#input").text("\"" + currentTask.input + "\"");
		$.mobile.changePage( "#sentimentTask", { transition: "slideup", changeHash: false, allowSamePageTransition: true });
	} else {
		$.mobile.changePage( "#loading", { transition: "slideup", changeHash: false, allowSamePageTransition: false });
	}
}

function setTaskResult(decision) {
	if (currentTask == null) {
		return;
	}
	currentTask.decision = decision;
	$.ajax({
		type: "POST",
		dataType: "json",
		data: JSON.stringify(currentTask),
		url: "/api/tasks",
		contentType: "application/json",
		processData: false,
		success: function( tasks ) {
		}
	});	
	if (queue.length != 0) {
		var task = queue.shift();
		setCurrentTask(task);
	} else {
		setCurrentTask(null);
	}
	
	if (queue.length <= MIN_QUEUE_LENGTH) {
		reloadQueue();
	}
}

function addToQueue(tasks) {
	for (var i = 0; i < tasks.length; i++) {
		var task = tasks[i];
		
		if (taskHistory.indexOf(task.id) != -1) {
			continue;
		}
		
		taskHistory.push(task.id);
		while (taskHistory.length > MAX_HISTORY) {
			taskHistory.shift()
		}
		
		if (queue.length == 0 && currentTask === null) {
			setCurrentTask(task);
		} else {
			var found = false;
			if (currentTask != null && currentTask.id == task.id) {
				found = true;
			}
			for (var j = 0; j < queue.length; j++) {
				if (queue[j].id == task.id) {
					found = true;
					break;
				}
			}
			if (!found) {
				queue.push(task);
			}
		}
	}
	
	// For app launch
	if (queue.length == 0 && currentTask == null) {
		setCurrentTask(null);
	}
	
	if (queue.length <= MIN_QUEUE_LENGTH) {
		window.setTimeout(function() {
     		reloadQueue();
   		}, 1000);
	}
}

function reloadQueue() {
	if (loading) {
		return;
	}
	
	url = "/api/tasks/assign?n=10";
	for (var i = 0; i < queue.length; i++) {
		var task = queue[i];
		url = url + "&veto=" + task.id;
	}
	
	$.ajax({
		dataType: "json",
		url: url,
		success: function( tasks ) {
			loading = false;
			addToQueue(tasks);
		},
		statusCode: {
    		401: function() {
      			window.location = '/';
	    	}
	    },
		error: function( ) {
			loading = false;
			window.setTimeout(function() {
     			reloadQueue();
   			}, 2000);
		}
	});
}

$( document ).ready(function() {

$( ".sentiment-button" ).click(function( event ) {
	var sentiment = $(this).data("value");
	setTaskResult(sentiment);
    event.preventDefault();
});

if (queue.length <= MIN_QUEUE_LENGTH) {
	reloadQueue();
}

});