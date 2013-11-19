var currentTask = null;

var queue = [];

var loading = false;

MIN_QUEUE_LENGTH = 5;

function setCurrentTask(task) {
	currentTask = task;
	if (currentTask != null) {
		$("#input").text(currentTask.input);
		$.mobile.changePage( "#sentimentTask", { transition: "slideup", changeHash: false, allowSamePageTransition: true });
	} else {
		$.mobile.changePage( "#loading", { transition: "slideup", changeHash: false, allowSamePageTransition: true });
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
		data: currentTask,
		url: "/api/tasks",
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
		if (queue.length == 0 && currentTask === null) {
			setCurrentTask(task);
		} else {
			queue.push(task);
		}
	}
}

function reloadQueue() {
	if (loading) {
		return;
	}
	
	url = "/api/tasks/assign?n=5";
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
		error: function() {
			loading = false;
			 window.setTimeout(function() {
     			reloadQueue();
   			}, 200);
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