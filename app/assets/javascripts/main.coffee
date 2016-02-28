$ ->
    alert("hi!")
    $(".options dt, .users dt").live "click", (e) ->
        e.preventDefault()
        if $(e.target).parent().hasClass("opened")
            $(e.target).parent().removeClass("opened")
        else
            $(e.target).parent().addClass("opened")
            $(document).one "click", ->
                $(e.target).parent().removeClass("opened")
        false

    $.fn.editInPlace = (method, options...) ->
        this.each ->
            methods =
                # public methods
                init: (options) ->
                    valid = (e) =>
                        newValue = @input.val()
                        options.onChange.call(options.context, newValue)
                    cancel = (e) =>
                        @el.show()
                        @input.hide()
                    @el = $(this).dblclick(methods.edit)
                    @input = $("<input type='text' />")
                        .insertBefore(@el)
                        .keyup (e) ->
                            switch(e.keyCode)
                                # Enter key
                                when 13 then $(this).blur()
                                # Escape key
                                when 27 then cancel(e)
                        .blur(valid)
                        .hide()
                edit: ->
                    @input
                        .val(@el.text())
                        .show()
                        .focus()
                        .select()
                    @el.hide()
                close: (newName) ->
                    @el.text(newName).show()
                    @input.hide()
            # jQuery approach: http://docs.jquery.com/Plugins/Authoring
            if ( methods[method] )
                return methods[ method ].apply(this, options)
            else if (typeof method == 'object')
                return methods.init.call(this, method)
            else
                $.error("Method " + method + " does not exist.")

    class Drawer extends Backbone.View
        initialize: ->
            @el.children("li").each (i,group) ->
                new Group
                    el: $(group)
                $("li",group).each (i,project) ->
                    new Project
                        el: $(project)
            $("#newGroup").click @addGroup
        addGroup: ->
            #$.ajax(jsRoutes.controllers.Projects.addGroup())
            alert("new group function!")
            jsRoutes.controllers.Projects.addGroup().ajax
                success: (data) ->
                    _view = new Group
                        el: $(data).appendTo("#projects")
                    _view.el.find(".groupName").editInPlace("edit")


    drawer = new Drawer el: $("#projects")

    class Group extends Backbone.View
        events:
            "click    .toggle"          : "toggle"
            "click    .newProject"      : "newProject"
        toggle: (e) ->
            e.preventDefault()
            @el.toggleClass("closed")
            false
        newProject: (e) ->
                @el.removeClass("closed")
            jsRoutes.controllers.Projects.add().ajax
                context: this
                data:
                    group: @el.attr("data-group")
                success: (tpl) ->
                    _list = $("ul",@el)
                    _view = new Project
                        el: $(tpl).appendTo(_list)
                    _view.el.find(".name").editInPlace("edit")
                error: (err) ->
                    $.error("Error: " + err)

    class Project extends Backbone.View
        initialize: ->
            @id = @el.attr("data-project")
            @name = $(".name", @el).editInPlace
                context: this
                onChange: @renameProject
        events:
            "click      .delete"        : "deleteProject"
        renameProject: (name) ->
            @loading(true)
            jsRoutes.controllers.Projects.rename(@id).ajax
                context: this
                data:
                    name: name
                success: (data) ->
                    @loading(false)
                    @name.editInPlace("close", data)
                error: (err) ->
                    @loading(false)
                    $.error("Error: " + err)
        loading: (display) ->
            if (display)
                @el.children(".delete").hide()
                @el.children(".loader").show()
            else
                @el.children(".delete").show()
                @el.children(".loader").hide()
