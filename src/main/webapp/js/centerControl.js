var centers = [
    {
        lat: 44.808,
        lng: 20.44,
        zoom: 13,
        name: "Beograd"
    }
];

function centerMap(i) {
    var c = centers[i];
    console.log(c);
    if (c !== undefined) {
        map.setCenter({lat: c.lat, lng: c.lng});
        map.setZoom(c.zoom);
    }
}

function updateCentersFromConfig() {
    centers = config.mapViews;
    var i;
    var cList = $("#centersList");
    cList.html("");
    for (i = 0; i < centers.length; i++) {
        var n = centers[i].name;
        var txt = '<li><a href="#" onclick="centerMap(' + i + ')">' + n + '</a></li>';
        cList.append(txt);
    }
}

function initCenterControl() {
    if (typeof (Storage) !== "undefined" && localStorage.centers) {
        centers = JSON.parse(localStorage.centers);
    }
    
    updateCentersFromConfig();

    $("#sacuvajPogledButton").click(function () {
        var name = $("#nazivPogledaInput").val();
        var c = map.getCenter();
        var z = map.getZoom();
        $("#nazivPogledaInput").val("");
        for(var i in centers){
            if(name === centers[i].name){
                $("#pogledDialogError").html("<br>Uneti naziv vec postoji");
                $("#pogledDialogError").removeClass("hidden");
                return false;
            }
        }
        if (name !== "") {
            var center = {
                lat: c.lat(),
                lng: c.lng(),
                zoom: z,
                name: name
            };
            centers.push(center);
            var i = centers.length - 1;
            var txt = '<li><a href="#" onclick="centerMap(' + i + ')">' + name + '</a></li>';
            $("#centersList").append(txt);

            $.post("AddViewServlet", $.param({center: JSON.stringify(center)}), function (responseText) {
                console.log(responseText);
            });

//            localStorage.centers = JSON.stringify(centers);
            $("#pogledDialogError").addClass("hidden");
        } else {
            $("#pogledDialogError").html("<br>Niste uneli naziv");
            $("#pogledDialogError").removeClass("hidden");
            return false;
        }
    });
    
    $("#dodajPogledModal").on('hidden.bs.modal', function () {
       $("#pogledDialogError").addClass("hidden");
    });

}