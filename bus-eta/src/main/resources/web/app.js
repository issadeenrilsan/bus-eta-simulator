
var REFRESH_SECONDS = 3;

var input = document.getElementById('stop');
var stopList = document.getElementById('stopList');
var servedBy = document.getElementById('served');
var statusLine = document.getElementById('status');
var liveDot = document.getElementById('liveDot');
var resultsTitle = document.getElementById('resultsTitle');
var resultsSection = document.getElementById('results');
var list = document.getElementById('buses');

var allStops = [];
var routesAtStop = {};
var chosenStop = '';
var matches = [];
var activeIndex = -1;
var timer = null;

// ---------- small helper: make an element with some text ----------
function el(tag, className, text) {
  var e = document.createElement(tag);
  if (className) e.className = className;
  if (text !== undefined) e.textContent = text;
  return e;
}

function showMessage(text) {
  list.innerHTML = '';
  list.appendChild(el('li', 'empty', text));
}


var root = document.documentElement;
var themeButton = document.getElementById('theme');

function applyTheme(name) {
  root.setAttribute('data-theme', name);
  themeButton.textContent = (name === 'dark') ? '☀' : '☾';
  themeButton.setAttribute('aria-label', name === 'dark' ? 'Switch to light mode' : 'Switch to dark mode');
}

var savedTheme = null;
try { savedTheme = localStorage.getItem('theme'); } catch (e) { /* storage is blocked: ignore */ }
var deviceLikesDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
applyTheme(savedTheme || (deviceLikesDark ? 'dark' : 'light'));

themeButton.addEventListener('click', function () {
  var next = (root.getAttribute('data-theme') === 'dark') ? 'light' : 'dark';
  applyTheme(next);
  try { localStorage.setItem('theme', next); } catch (e) { /* ignore */ }
});


function loadStops() {
  return fetch('/api/stops')
    .then(function (res) { return res.json(); })
    .then(function (stops) {
      var stopsPerRoute = {};   // route name -> how many stops it has
      var routeOrder = [];

      stops.forEach(function (stop) {
        routesAtStop[stop.name] = stop.routes;
        stop.routes.forEach(function (routeName) {
          if (stopsPerRoute[routeName] === undefined) {
            stopsPerRoute[routeName] = 0;
            routeOrder.push(routeName);
          }
          stopsPerRoute[routeName]++;
        });
      });

      stops.sort(function (a, b) { return a.name.localeCompare(b.name); });
      allStops = stops;

      var routeList = document.getElementById('routeList');
      routeOrder.forEach(function (routeName) {
        var item = el('li');
        item.appendChild(el('b', '', routeName));
        item.appendChild(el('small', '', stopsPerRoute[routeName] + ' stops'));
        routeList.appendChild(item);
      });
    })
    .catch(function () {
      showMessage('Cannot reach the server. Is the Java program running?');
    });
}


function findMatches(typed) {
  var q = typed.trim().toLowerCase();
  if (q === '') return allStops.slice();      // nothing typed: show every stop

  var startsWith = [];
  var contains = [];
  allStops.forEach(function (stop) {
    var at = stop.name.toLowerCase().indexOf(q);
    if (at === 0) startsWith.push(stop);
    else if (at > 0) contains.push(stop);
  });
  return startsWith.concat(contains);
}


function findExact(typed) {
  var q = typed.trim().toLowerCase();
  for (var i = 0; i < allStops.length; i++) {
    if (allStops[i].name.toLowerCase() === q) return allStops[i];
  }
  return null;
}


function nameWithHighlight(name, typed) {
  var span = el('span', 'stop-name');
  var q = typed.trim().toLowerCase();
  var at = (q === '') ? -1 : name.toLowerCase().indexOf(q);

  if (at < 0) {
    span.textContent = name;
    return span;
  }
  span.appendChild(document.createTextNode(name.substring(0, at)));
  span.appendChild(el('mark', '', name.substring(at, at + q.length)));
  span.appendChild(document.createTextNode(name.substring(at + q.length)));
  return span;
}


function showList() {

  var typed = (input.value === chosenStop) ? '' : input.value;

  matches = findMatches(typed);
  activeIndex = (typed.trim() !== '' && matches.length > 0) ? 0 : -1;   // Enter picks the first match

  stopList.innerHTML = '';
  if (matches.length === 0) {
    stopList.appendChild(el('li', 'stop-none', 'No stop found. Try fewer letters.'));
  }

  matches.forEach(function (stop, i) {
    var item = el('li', 'stop-item');
    item.id = 'stop-option-' + i;
    item.setAttribute('role', 'option');
    item.appendChild(nameWithHighlight(stop.name, typed));
    item.appendChild(el('small', '', stop.routes.join(' · ')));


    item.addEventListener('mousedown', function (event) {
      event.preventDefault();
      chooseStop(stop.name, true);
    });
    stopList.appendChild(item);
  });

  stopList.hidden = false;
  input.setAttribute('aria-expanded', 'true');
  markActive();
}

function closeList() {
  stopList.hidden = true;
  input.setAttribute('aria-expanded', 'false');
}


function markActive() {
  var items = stopList.querySelectorAll('.stop-item');
  for (var i = 0; i < items.length; i++) {
    var isActive = (i === activeIndex);
    items[i].classList.toggle('active', isActive);
    items[i].setAttribute('aria-selected', isActive ? 'true' : 'false');
  }

  if (activeIndex >= 0 && items[activeIndex]) {
    var item = items[activeIndex];
    input.setAttribute('aria-activedescendant', item.id);
    if (item.offsetTop < stopList.scrollTop) {
      stopList.scrollTop = item.offsetTop;
    } else if (item.offsetTop + item.offsetHeight > stopList.scrollTop + stopList.clientHeight) {
      stopList.scrollTop = item.offsetTop + item.offsetHeight - stopList.clientHeight;
    }
  } else {
    input.removeAttribute('aria-activedescendant');
  }
}

function onKey(event) {
  if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
    event.preventDefault();
    if (stopList.hidden) {
      showList();
      return;
    }
    if (matches.length === 0) return;

    var step = (event.key === 'ArrowDown') ? 1 : -1;
    if (activeIndex === -1) {
      activeIndex = (step === 1) ? 0 : matches.length - 1;
    } else {
      activeIndex = (activeIndex + step + matches.length) % matches.length;
    }
    markActive();

  } else if (event.key === 'Enter') {
    if (!stopList.hidden && activeIndex >= 0) {
      event.preventDefault();                       // do not also submit the form
      chooseStop(matches[activeIndex].name, true);
    }

  } else if (event.key === 'Escape') {
    closeList();
  }
}

input.addEventListener('input', function () {
  if (input.value.trim() === '' && chosenStop !== '') {
    clearSelection();                               // the passenger emptied the box
  }
  showList();
});
input.addEventListener('focus', function () { input.select(); showList(); });
input.addEventListener('click', function () { if (stopList.hidden) showList(); });
input.addEventListener('blur', closeList);
input.addEventListener('keydown', onKey);


document.getElementById('search').addEventListener('submit', function (event) {
  event.preventDefault();                           // do not reload the page

  var typed = input.value.trim();
  if (typed === '') {
    showList();                                     // nothing typed: show every stop to pick from
    return;
  }

  var exact = findExact(typed);
  if (exact) {
    chooseStop(exact.name, true);
    return;
  }

  var found = findMatches(typed);
  if (found.length === 1) {
    chooseStop(found[0].name, true);                // only one stop fits what was typed
  } else {
    showList();                                     // several (or no) stops fit: let the passenger pick
  }
});


function chooseStop(name, scrollToResults) {
  clearInterval(timer);
  chosenStop = name;
  input.value = name;
  closeList();
  input.blur();                                     // on a phone this hides the keyboard

  list.innerHTML = '';
  statusLine.textContent = '';
  liveDot.hidden = true;
  servedBy.textContent = 'Routes that stop here: ' + routesAtStop[name].join(' · ');
  resultsTitle.textContent = 'Buses coming to ' + name;

  refresh();
  timer = setInterval(refresh, REFRESH_SECONDS * 1000);

  if (scrollToResults && resultsSection.scrollIntoView) {
    resultsSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }
}

function clearSelection() {
  clearInterval(timer);
  chosenStop = '';
  servedBy.textContent = '';
  statusLine.textContent = '';
  liveDot.hidden = true;
  resultsTitle.textContent = 'Buses coming to your stop';
  showMessage('Search for your stop above to see the buses coming to it.');
}


function refresh() {
  var stopName = chosenStop;
  if (stopName === '') return;

  fetch('/api/arrivals?stop=' + encodeURIComponent(stopName))
    .then(function (res) {
      if (!res.ok) throw new Error('HTTP ' + res.status);
      return res.json();
    })
    .then(function (buses) {
      if (chosenStop !== stopName) return;          // the passenger picked another stop meanwhile
      draw(buses);
      liveDot.hidden = false;
      statusLine.textContent = 'Live · updated ' + new Date().toLocaleTimeString();
    })
    .catch(function () {
      liveDot.hidden = true;
      statusLine.textContent = 'Cannot reach the server. Trying again…';
    });
}


function draw(buses) {
  list.innerHTML = '';
  if (buses.length === 0) {
    showMessage('No bus is on its way to this stop right now. Buses leave every minute, so check again shortly.');
    return;
  }
  buses.forEach(function (bus) {
    list.appendChild(makeCard(bus));
  });
}

function makeCard(bus) {
  var soon = bus.eta_minutes < 2;
  var card = el('li', soon ? 'card soon' : 'card');

  // left: the waiting time
  var eta = el('div', 'eta');
  if (bus.eta_minutes < 1) {
    eta.appendChild(el('span', 'num word', 'Due'));
  } else {
    eta.appendChild(el('span', 'num', String(Math.round(bus.eta_minutes))));
    eta.appendChild(el('span', 'unit', 'min'));
  }


  var info = el('div', 'info');
  info.appendChild(el('div', 'name', bus.bus_name));
  info.appendChild(el('span', 'route', bus.route));

  var detail = el('div', 'detail');
  if (soon) {
    detail.appendChild(el('strong', '', 'Arriving soon · '));
  }
  detail.appendChild(document.createTextNode(
    'going to ' + bus.going_to + ' · ' + bus.distance_km.toFixed(1) + ' km away'));
  info.appendChild(detail);

  card.appendChild(eta);
  card.appendChild(info);
  return card;
}

loadStops().then(function () {

  var wanted = new URLSearchParams(window.location.search).get('stop');
  var wantedStop = wanted ? findExact(wanted) : null;

  if (wantedStop) {
    chooseStop(wantedStop.name, true);
  } else {
    showMessage('Search for your stop above to see the buses coming to it.');
  }
});
