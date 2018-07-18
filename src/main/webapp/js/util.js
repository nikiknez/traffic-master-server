
function setToCenter(element, dim) {
    var w = $(window).width();
    var h = $(window).height();

    if(dim.width / dim.height > w / h){
        element.left = 0;
        element.width = w;
        element.height = w * dim.height / dim.width;
        element.top = (h - element.height) / 2;
    }else{
        element.top = 0;
        element.height = h;
        element.width = h * dim.width / dim.height;
        element.left = (w - element.width) / 2;
    }
}

function moveElementTo(element, x, y) {
    var eW = element.width();
    var eH = element.height();
    var bodyW = $(document.body).width();
    var bodyH = $(document.body).height();
    
    if (x + eW < bodyW) {
        element.css("left", x);
    } else {
        element.css("left", bodyW - eW);
    }
    if (y + eH < bodyH) {
        element.css("top", y);
    } else {
        element.css("top", bodyH - eH);
    }
}

function intensityToColorMap(intensity) {
    if (intensity > 10) {
        return 'green';
    }
    if (intensity > 5) {
        return 'yellow';
    }
    return 'red';
}