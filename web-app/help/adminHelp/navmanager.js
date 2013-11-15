function d2hNavigationManager(frmLeft, frmRight)
{
    this.Left = frmLeft;
    this.Right = frmRight;
    this.NavSet = frmRight.parentElement ? frmRight.parentElement : frmRight.parentNode;
    this._panesSize = this.NavSet.cols;
    this._handleOnResize = true;
    this._needSyncTOC = false;
    this._prevActivePane = null;
    
    
    d2hNavigationManager.prototype.HideNavigationPane = function()
    {
        this._handleOnResize = false;
        this._panesSize = this.NavSet.cols;
        var leftDoc = getFrameDocument(this.Left);
        if (leftDoc && leftDoc.body && leftDoc.body.offsetWidth > 0)
            this._panesSize = leftDoc.body.offsetWidth + ",*";
        this.NavSet.cols = "0,*";
        this._prevActivePane = this.PressNavigationButton("D2HNone");
        this._handleOnResize = true;
    }
    
    d2hNavigationManager.prototype.ShowNavigationPane = function()
    {
        if (!this._panesSize)
            return;
        this._handleOnResize = false;
        this.NavSet.cols = this._panesSize;
        this._panesSize = null;
        this._prevActivePane = null;
        this._handleOnResize = true;
    }
    
    d2hNavigationManager.prototype._onResizeLeft = function()
    {
        if (!this._handleOnResize)
            return;
        var leftDoc = getFrameDocument(this.Left)
        if (leftDoc && leftDoc.body)
        {
            if (leftDoc.body.offsetWidth > 0)
            {
                this._panesSize = leftDoc.body.offsetWidth + ",*";
                if (this.NavSet.cols != "0,*" && this._prevActivePane)
                {
                    this.PressNavigationButton(this._prevActivePane);
                    this._prevActivePane = null;
                }
            }
        }
    }
    
    d2hNavigationManager.prototype.SyncTOC = function(scrollByHorizontal)
    {
        var doc = getFrameDocument(this.Left);
        if (doc)
        {
            var wnd = getWindow(doc);
            if (wnd && wnd.d2hSyncTOC)
                wnd.d2hSyncTOC(scrollByHorizontal);
        }
    }
    
    d2hNavigationManager.prototype.NeedSyncTOC = function(val)
    {
        this._needSyncTOC = val;    
    }
    
    d2hNavigationManager.prototype.PerformSyncTOC = function()
    {
        if (this._needSyncTOC)
        {
            this.SyncTOC();
            this._needSyncTOC = false;
            return true;
        }
        return false;
    }
    
    d2hNavigationManager.prototype.PressNavigationButton = function(buttonId)
    {
        var doc = getFrameDocument(this.Left);
        if (!doc)
            return null;
        var wnd = getWindow(doc);
        if (!wnd || !wnd.d2hPressPaneButton)
        {
            doc = getFrameDocument(this.Right);
            wnd = getWindow(doc);
            if (!wnd || !wnd.d2hPressPaneButton)
                return null;
        }
        var res = wnd.d2hActivePaneID();
        if (!buttonId)
            buttonId = res;
        wnd.d2hPressPaneButton(buttonId);
        return res;
    }
 }

function d2hFavItem(title, relPath)
{
    this.Title = title;
    this.Url = relPath;
    this.RowId;
    
    d2hFavItem.prototype.CreateRow = function(doc, rowId, target)
    {
        this.RowId = rowId;
        var row = doc.createElement("tr");
        row.setAttribute("id", "tr_" + rowId);
        var td = doc.createElement("td");
        td.setAttribute("nowrap", 1);
        var a = doc.createElement("a");
        a.setAttribute("href", this.Url);
        var name = "atr_" + rowId;
        a.setAttribute("id", name);
        a.setAttribute("name", name);
        if (target)
            a.setAttribute("target", target);
        a.innerHTML = this.Title;
        var wnd = getWindow(doc);
        if (!wnd)
            wnd = window;
        a.onmouseover = wnd.d2hItemOver;
        a.onmouseout = wnd.d2hItemOut;
        a.onclick = wnd.d2hItemSelect;
        td.appendChild(a);
        row.appendChild(td);
        td = doc.createElement("td");
        td.setAttribute("nowrap", 1);
        td.setAttribute("width", "10px");
        td.setAttribute("style", "padding-left: 6pt; text-align: center");
        var sp = doc.createElement("span");
        sp.innerHTML = "&nbsp;";
        td.appendChild(sp);
        a = doc.createElement("a");
        var str = "javascript:d2hFavoriteRemove(\"tr_" + rowId +"\");"        
        a.setAttribute("href", str);
        a.setAttribute("id", name+"_del");
        a.innerHTML = wnd.d2hGetFavoritesRemove();
        a.onmouseover = wnd.d2hItemOver;
        a.onmouseout = wnd.d2hItemOut;
        td.appendChild(a);
        row.appendChild(td);        
        return row;
    }
}

function d2hFavorites(prjID, wnd)
{
    this._project = prjID;
    this.Window = wnd;
    this._items = new Array();
    this._selectedItem = null;
    
    d2hFavorites.prototype.HasProjectID = function()
    {
        return this._project && this._project.length > 0;
    }
    
    d2hFavorites.prototype.SetProjectID = function(prjId)
    {
        this._project = prjId;
    }

    d2hFavorites.prototype.Load = function(handler)
    {
        cookie = this.Window.d2hGetCookie(this._project);
        if (!cookie)
            cookie = "";
        var favorites = cookie.split("||");
        if (favorites)
        {
            var pair;
            for (i = 0; i < favorites.length; i++)
            {
                pair = favorites[i].split("|");
                if (pair && pair.length > 1)
                    this.AddItem(this.Window.d2hDecodeURIComponent(pair[1]), this.Window.d2hDecodeURIComponent(pair[0]), handler);
            }
        }
    }
    
    d2hFavorites.prototype.AddItem = function(title, relPath, handler)
    {
        if (!relPath)
            return false;
        if (this._items[relPath])
            return false;
        this._items[relPath] = new d2hFavItem(title, relPath);
        if (handler)
            handler(this._items[relPath]);
        return true;
    }

    d2hFavorites.prototype.Add = function(title, relPath, addHandler, selHandler)
    {
        if (this.AddItem(title, relPath, addHandler))
            this.AddToCookies(title, relPath);
        this._selectedItem = this._items[relPath];
        if (selHandler)
            selHandler(this.GetLastAddedItem());
    }
    
    d2hFavorites.prototype.Remove = function(relPath)
    {
        var item = this._items[relPath];
        if (item)
        {
            this.RemoveFromCookies(item.Title, item.Url);
            if (this._selectedItem == this._items[relPath])
                this._selectedItem = null;
            this._items[relPath] = null;
        }
    }
    
    d2hFavorites.prototype.Populate = function(handler)
    {
        if (!handler)
            return;
        for (var i in this._items)
            if (this._items[i])
                handler(this._items[i]);
    }
    
    d2hFavorites.prototype.AddToCookies = function(title, relPath)
    {
        var cookie = this.Window.d2hGetCookie(this._project);
        if (cookie)
            cookie += "||";
        else
            cookie = "";
        cookie += this.Window.d2hEncodeURIComponent(relPath) + "|" + this.Window.d2hEncodeURIComponent(title);
        var expires = new Date();
        expires = new Date(expires.getTime() + (1000 * 60 * 60 * 24 * 365));
        this.Window.d2hSetCookie(this._project, cookie, null, expires.toGMTString(), null, null);
    }
    
    d2hFavorites.prototype.RemoveFromCookies = function(title, relPath)
    {
        var cookie = this.Window.d2hGetCookie(this._project);
        if (!cookie)
            return;
        var template = this.Window.d2hEncodeURIComponent(relPath) + "|" + this.Window.d2hEncodeURIComponent(title);
        var indx = cookie.indexOf(template);
        if (indx == -1)
            return;
        var len = template.length;
        if (indx > 2 && cookie.substring(indx - 2, indx) == "||")
        {
            len += 2;
            indx -= 2;
        }
        var str1 = cookie.substring(0, indx);
        var str2 = cookie.substring(indx + len, cookie.length - 1);
        cookie = str1 + str2;
        expires = new Date();
        expires = new Date(expires.getTime() + (1000 * 60 * 60 * 24 * 365));
        this.Window.d2hSetCookie(this._project, cookie, null, expires.toGMTString(), null, null);
    }
    
    d2hFavorites.prototype.GetLastAddedItem = function()
    {
        var item = this._selectedItem;
        this._selectedItem = null;
        return item;
    }
}