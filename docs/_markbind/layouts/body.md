<div id="flex-body">
{% if file %}
<nav id="site-nav" class="fixed-header-padding">
<div class="site-nav-top">
<div class="font-weight-bold mb-2" style="font-size: 1.25rem;"><md>****{{ sitenav_title }}****</md></div>
</div>
<div class="nav-component slim-scroll">
<include src="{{ file }}" />
</div>
</nav>
{% endif %}
<div id="content-wrapper" class="fixed-header-padding">
{{ content }}
</div>
{% if pagnav %}
<nav id="page-nav" class="fixed-header-padding">
<div class="nav-component slim-scroll">
<page-nav />
</div>
</nav>
{% endif %}
</div>
