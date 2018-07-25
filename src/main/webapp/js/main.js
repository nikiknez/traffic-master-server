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
//    var trafficLayer = new google.maps.TrafficLayer();
//    trafficLayer.setMap(map);

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

    setupTrafficDataFetching();
    
    setupConfigFetching();

    for (var i in config.marks) {
        var m = config.marks[i];
        new MarkedPoint(m.owner, m.location, m.info, m.validFrom, m.validTo, m.id);
    }
});

var mobileStreetPaths = [];
var showMobileData = true;
function displayMobileData(visible) {
    showMobileData = visible;
    for (var p in mobileStreetPaths) {
        mobileStreetPaths[p].setVisible(visible);
    }
}
function updateMobileStreetData(streetId, streetData) {
    var po = {
        map: map,
        strokeWeight: 3,
        strokeColor: intensityToColorMap(streetData.intensity),
        path: streetData.path,
        visible: showMobileData
    };
    var streetPolyLine = mobileStreetPaths[streetId];
    if (streetPolyLine) {
        streetPolyLine.setOptions(po);
    } else {
        mobileStreetPaths[streetId] = new google.maps.Polyline(po);
    }
}
function setupTrafficDataFetching() {
    function getTraficData() {
        $.get("GetTrafficDataServlet", function (data) {
            for (var source in data) {
                var streetsData = data[source].data;
                for (var streetId in streetsData) {
                    var streetData = streetsData[streetId];
//                    console.log(streetId + ": " + streetData.intensity);
                    var s = streets[streetId];
                    if (s) {
                        s.setStreetData(streetData);
                    } else {
                        // (mobile data)
                        updateMobileStreetData(streetId, streetData);
                    }
                }
            }
        }).always(function () {
//            setTimeout(getTraficData, 5000);
        });
    }
    setTimeout(getTraficData, 3000);
}

function setupConfigFetching() {
    function getConfig() {
        $.get("GetConfigurationServlet", function (newConfig) {
            console.log(newConfig);
            config = newConfig;
            updateCentersFromConfig();
            updateStreetsFromConfig();
            updateCamerasFromConfig();
        }).always(function () {
            setTimeout(getConfig, 20000);
        });
    }
    
    setTimeout(getConfig, 20000);
}

function mapRightClickCallback(event) {
    if (drawingMode || streetSelectMode || camSelectMode) {
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

function drawBounds(placeId) {
    var geocoder = new google.maps.Geocoder();
    placeId = placeId || "ChIJvTbSImJlWkcRj7p3QMKD84Q";
    geocoder.geocode({'placeId': placeId}, function (results, status) {
        if (status !== 'OK') {
            window.alert('Geocoder failed due to: ' + status);
            return;
        }
        var b = results[0].geometry.bounds;
        console.log(b);

        new google.maps.Rectangle({
            map: map,
            bounds: {north: b.f.b, south: b.f.f, west: b.b.b, east: b.b.f}
        });

    });
}