package main

import (
	"os"
	"time"
	"sync"
	"sync/atomic"
	"encoding/json"
	"strings"
	"net/url"
	"net/http"
	"io/ioutil"
	//"log"
	//"reflect"
	"github.com/mssola/user_agent"
)

import "fmt"

const URL = "http://metrics.pfizer.com/metrics/servlet/RegisterEventServlet"

// If there are more than this number of unprocessed messages, further messages will be dropped until the server
// catches up.
const MAX_QUEUE_LENGTH = 1000


var wg sync.WaitGroup
// The internal count of WaitGroup is not accessible, so we maintain another copy to know how many requests are in progress
var requests_in_progress int32 = 0

func atomicAdd(i *int32, delta int32) { atomic.AddInt32(i, delta) }
func atomicLoad(i *int32) int32 { return atomic.LoadInt32(i) }


//type jsonMsg map[string]interface{}

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


func removeEmpty(in []string) []string {
	res := []string{}
	for _, i := range in {
		if i != "" {
			res = append(res, i)
		}
	}
	return res
}

func send_auditlog_record(msg jsonMsg) {

	//fmt.Println(msg)
	params := url.Values{}

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
		ua := user_agent.New(msg.Browser)
		browser, version := ua.Browser()
		params.Set("browser", browser + " " + version)
	}

//	for k, v := range msg {
//		params.Set(k, fmt.Sprintf("%v", v))
//	}

	fullurl := URL + "?" + params.Encode()

	fmt.Println(fullurl)
	
	resp, err := http.PostForm(fullurl, url.Values{})
	if err != nil {
		error("WARNING: Failed to post message to metrics server: "+err.Error())
	} else {
		defer resp.Body.Close()
		ioutil.ReadAll(resp.Body)
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
	msgp := &msg

	for dec.More() {
		err := dec.Decode(&msgp)
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

