var newStreetPolyline;
var drawingMode = false;
var streets = [];
function initStreetDrawing() {
    
    for(var i in config.streets){
        var s = config.streets[i];
        var gpoly = new google.maps.Polyline({
            path: s.path,
            map: map
        });
        var street = new MarkedStreet(gpoly, s);
        streets[street.id] = street;
    }

    $("#markStreetContextButton").click(function () {
        newStreetPolyline = new google.maps.Polyline({
            path: [],
            editable: true,
            map: map
        });
        var path = newStreetPolyline.getPath();

        newStreetPolyline.addListener('rightclick', function (event) {
            console.log(event);
            if (event.vertex !== undefined) {
                path.removeAt(event.vertex);
            }
        });

        enterDrawingMode();
    });

    $("#saveStreetButton").click(function () {
        newStreetPolyline.setOptions({editable: false});
        google.maps.event.clearListeners(newStreetPolyline, 'rightclick');
        exitDrawingMode();

        var street = new MarkedStreet(newStreetPolyline);
        
        var p = {path: JSON.stringify(newStreetPolyline.getPath().b)};
        $.post("AddStreetServlet", $.param(p), function (response) {
            console.log(response);
            street.owner = response.owner;
            street.id = response.id;
            streets[street.id] = street;
        });
        newStreetPolyline = null;
    });

    $("#cancleStreetButton").click(function () {
        newStreetPolyline.setMap(null);
        exitDrawingMode();
        newStreetPolyline = null;
    });

    function mapClickCallback(event) {
        if (drawingMode) {
            var path = newStreetPolyline.getPath();
            path.push(event.latLng);
        }
    }
    google.maps.event.addListener(map, 'click', mapClickCallback);
    function enterDrawingMode() {
        drawingMode = true;
        map.setOptions({disableDefaultUI: true});
        $("#centerControlDiv").hide();
        $("#userControlDiv").hide();
        $("#streetControlDiv").removeClass("hidden");
        $("#info1").removeClass("hidden");
        map.setOptions({
            draggableCursor: 'crosshair'
        });
    }
    function exitDrawingMode() {
        drawingMode = false;
        map.setOptions({disableDefaultUI: false});
        $("#centerControlDiv").show();
        $("#userControlDiv").show();
        $("#streetControlDiv").addClass("hidden");
        $("#info1").addClass("hidden");
        map.setOptions({
            draggableCursor: 'default'
        });
    }
}