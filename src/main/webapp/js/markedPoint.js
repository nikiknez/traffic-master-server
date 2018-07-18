var marks = [];

function MarkedPoint(text, validFrom, validTo) {
    var self = this;
    this.infoText = text;
    this.validFrom = validFrom;
    this.validTo = validTo;
    this.location = lastClickLocation;
    this.owner = currentUser.username;

    var p = {info: text, from: validFrom, to: validTo};
    p.location = JSON.stringify(lastClickLocation);
    $.post("AddMarkServlet", $.param(p), function (response) {
        self.id = response;
        marks.push(this);
    });

    var marker = new google.maps.Marker({
        position: lastClickLocation,
        map: map,
        icon: 'icons/warning.png',
        title: text
    });
    var iw = new google.maps.InfoWindow();
    iw.setOptions({maxWidth: 300});

    marker.addListener('click', function (e) {
        iw.setContent(text);
        iw.open(map, marker);
    });

    marker.addListener('rightclick', openContextMenu);

    function openContextMenu(event) {
        $(".contextMenu").addClass("hidden");
        if (camSelectMode || drawingMode || streetSelectMode) {
            return;
        }
        if (!currentUser || currentUser.username !== self.owner) {
            return;
        }
        var cMenu = $("#markPointContextMenu");
        console.log(event.Ha);
        cMenu.removeClass("hidden");
        moveElementTo(cMenu, event.Ha.x, event.Ha.y);
    }
}
