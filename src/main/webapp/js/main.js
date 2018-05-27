var map;

var lastLocation = {
    lat: 44.808,
    lng: 20.44
};
var zoom = 13;

var lastClickLocation;

$(document).ready(function () {
    if (typeof (Storage) !== "undefined") {
        if (localStorage.zoom) {
            zoom = parseFloat(localStorage.zoom);
        }
        if (localStorage.lastLocation) {
            lastLocation = JSON.parse(localStorage.lastLocation);
        }
    }
    map = new google.maps.Map(document.getElementById('map'), {
        center: lastLocation, zoom: zoom, draggableCursor: 'default',
        mapTypeControlOptions: {
            mapTypeIds: ['roadmap', 'satellite']
        }
    });
//    map.mapTypes.satellite.name = 'Satelit';
//    map.mapTypes.roadmap.name = 'Mapa';
//    map.setOptions({'mapTypeControl': true});

    if (typeof (Storage) !== "undefined") {
        var loop;
        map.addListener('bounds_changed', function () {
            clearTimeout(loop);
            loop = setTimeout(function () {
                lastLocation.lat = map.getCenter().lat();
                lastLocation.lng = map.getCenter().lng();
                zoom = map.getZoom();
                localStorage.lastLocation = JSON.stringify(lastLocation);
                localStorage.zoom = zoom;
            }, 500);

        });
    }

    map.addListener("rightclick", mapRightClickCallback);
    
    $("body").contextmenu(function (e) {
        e.preventDefault();
        e.stopPropagation();
        return false;
    });

    $(document.body).click(function () {
        $(".contextMenu").addClass("hidden");
    });
    
    initCenterControl();

    initUserControl();

    initStreetDrawing();

    initCameraSetup();
    
    initTrafficDataFetching();
});

function initTrafficDataFetching() {
    function getTraficData () {
        $.get("GetTrafficDataServlet", function(data) {
            for(var source in data) {
                for(var street in data[source].data) {
                    console.log(street + ": " + data[source].data[street].intensity);
                }
            }
            setTimeout(getTraficData, 3000);
        });
    }
    setTimeout(getTraficData, 3000);
}

function mapRightClickCallback(event) {
    if(drawingMode || streetSelectMode || camSelectMode){
        return;
    }
    $(".contextMenu").addClass("hidden");
    
    var cMenu = $("#contextMenu");
    cMenu.removeClass("hidden");

    lastClickLocation = event.latLng;

    var cMenuW = cMenu.width();
    var cMenuH = cMenu.height();
    var bodyW = $(document.body).width();
    var bodyH = $(document.body).height();

    if (event.pixel.x + cMenuW < bodyW) {
        cMenu.css("left", event.pixel.x);
    } else {
        cMenu.css("left", bodyW - cMenuW);
    }

    if (event.pixel.y + cMenuH < bodyH) {
        cMenu.css("top", event.pixel.y);
    } else {
        cMenu.css("top", bodyH - cMenuH);
    }
    return false;
}

function reload_js(src) {
    $('script[src="js/' + src + '.js"]').remove();
    $('<script>').attr('src', 'js/' + src + '.js').appendTo('head');
}
