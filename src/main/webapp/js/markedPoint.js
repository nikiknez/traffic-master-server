var marks = [];
var selectedMark = null;
function MarkedPoint(owner, location, text, validFrom, validTo, id) {
    var self = this;
    this.info = text;
    this.validFrom = validFrom;
    this.validTo = validTo;
    this.location = location;
    this.owner = owner;
    this.id = id;

    if (id) {
        marks[id] = self;
    }else {
        // new mark just created
        var d = {info: text, validFrom: validFrom, validTo: validTo,
            owner: this.owner, location: location};
        $.post("AddRemoveMarkServlet", $.param({add: 1, data: JSON.stringify(d)}), function (id) {
            self.id = id;
            marks[id] = self;
        });
    }

    var marker = new google.maps.Marker({
        position: location,
        map: map,
        icon: 'icons/warning.png',
        title: text
    });
    var iw = new google.maps.InfoWindow();
    iw.setOptions({maxWidth: 300});
    iw.setContent(text);

    marker.addListener('click', function (e) {
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
        selectedMark = self;
        var cMenu = $("#markPointContextMenu");
        console.log(event.Ha);
        cMenu.removeClass("hidden");
        moveElementTo(cMenu, event.Ha.x, event.Ha.y);
    }

    self.remove = function () {
        marker.setMap(null);
        iw.close();
        marks[self.id] = undefined;
        $.post("AddRemoveMarkServlet", {id: self.id});
    };

    self.hideIfNotMine = function () {
        marker.setVisible(self.owner === currentUser.username);
        iw.close();
    };

    self.show = function () {
        marker.setVisible(true);
    };
}

$("#removeMarkPointButton").click(function () {
    if (selectedMark) {
        selectedMark.remove();
        selectedMark = null;
    }
});
