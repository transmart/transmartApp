/*
to build: install go tools,
> go get github.com/mssola/user_agent
> go build pfizer_audit_logger.go

This script will read json-encoded auditlog messages from Transmart and send them as HTTP POST messages to
metrics.pfizer.com. The input must be provided as json objects on stdin. If a request fails this is noted
on stderr, but the request is not retried so in that case the message is lost. If more than MAX_QUEUE_LENGTH
http operations are in progress at the same time any further input messages are ignored until some http
operations finish.
*/

package main

import (
	"os"
	"time"
	"sync"
	"sync/atomic"
	"encoding/json"
	"fmt"
	"strings"
	"net/url"
	"net/http"
	"io/ioutil"
	"github.com/mssola/user_agent"
)

const URL = "http://metrics.pfizer.com/metrics/servlet/RegisterEventServlet"

// If there are more than this number of unprocessed messages, further messages will be dropped until the server
// catches up.
const MAX_QUEUE_LENGTH = 1000


var wg sync.WaitGroup
// The internal count of WaitGroup is not accessible, so we maintain another copy to know how many requests are in progress
var requests_in_progress int32 = 0

func atomicAdd(i *int32, delta int32) { atomic.AddInt32(i, delta) }
func atomicLoad(i *int32) int32 { return atomic.LoadInt32(i) }


/* struct based implementation */
type jsonMsg struct {
       Action string
       Study string
       Subset1 string
       Subset2 string
       Application string `json:"program"`
       AppVersion string `json:"programVersion"`
       User string
       Task string `json:"event"`
       Browser string `json:"userAgent"`
}
/**/

/* Alternative map based implementation 
type jsonMsg map[string]string

var keyMap = map[string]string{
	"program": "application",
	"programVersion": "appVersion",
	"event": "task",
}
*/


func send_auditlog_record(msg jsonMsg) {

	//fmt.Println(msg)
	params := url.Values{}

/* struct based implementation */

	if msg.Action != "" {
		params.Set("action", msg.Action)
	} else {
		action := []string{}
		for _, s := range []string{msg.Study, msg.Subset1, msg.Subset2} {
			if s != "0" && s != "" {
				action = append(action, s)
			}
		}
		params.Set("action", strings.Join(action, "|"))
	}
	if msg.Application != "" { params.Set("application", msg.Application) }
	if msg.AppVersion != "" { params.Set("appVersion", msg.AppVersion) }
	if msg.User != "" { params.Set("user", msg.User) }
	if msg.Task != "" { params.Set("task", msg.Task) }
	if msg.Browser != "" {
		browser, version := user_agent.New(msg.Browser).Browser()
		params.Set("browser", browser + " " + version)
	}
/**/

/* alternative map based implementation 

	for _, k := range []string{"action", "program", "programVersion", "user", "event"} {
		if _, hasKey := msg[k]; !hasKey { continue }
		key := k
		if outkey, translate := keyMap[k]; translate {
			key = outkey
		}
		params.Set(key, fmt.Sprintf("%v", msg[k]))
	}
	if _, hasKey := msg["action"]; !hasKey {
		action := []string{}
		for _, s := range []string{msg["study"], msg["subset1"], msg["subset2"]} {
			if s != "0" && s != "" {
				action = append(action, s)
			}
		}
		params.Set("action", strings.Join(action, "|"))
	}
	if _, hasKey := msg["userAgent"]; hasKey {
		browser, version := user_agent.New(msg["userAgent"]).Browser()
		params.Set("browser", browser + " " + version)
	}
*/

	fullurl := URL + "?" + params.Encode()

	//fmt.Println(fullurl)
	resp, err := http.PostForm(fullurl, url.Values{})
	if err != nil {
		error("WARNING: Failed to post message to metrics server: "+err.Error())
	} else {
		ioutil.ReadAll(resp.Body)
		resp.Body.Close()
	}

	atomicAdd(&requests_in_progress, -1)
	wg.Done()
}


func error(m string) {
	fmt.Fprintln(os.Stderr, time.Now().Format("2006-01-02 15:04:05.000-07:00  ") + m)
}

func fatal(m string) {
	error(m)
	os.Exit(1)
}


func main() {
	
	dec := json.NewDecoder(os.Stdin)
	msg := jsonMsg{}

	for dec.More() {
		err := dec.Decode(&msg)
		if err != nil { fatal("ERROR: Failed to parse JSON message from stdin: " + err.Error()) }
		if atomicLoad(&requests_in_progress) > MAX_QUEUE_LENGTH {
			line, _ := json.Marshal(msg)
			error("WARNING: Too many unfinished requests in progress, ignoring message "+string(line))
			continue
		}
		atomicAdd(&requests_in_progress, 1)
		wg.Add(1)
		go send_auditlog_record(msg)
		msg = jsonMsg{}
	}

	//fmt.Println("done reading")
	wg.Wait()
	//fmt.Println("Exiting")
}
